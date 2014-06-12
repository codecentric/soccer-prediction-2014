package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.text.ParseException;
import java.util.*;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;
import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

public class Matches implements Iterable<Match>{

	private List<Match> matches = new ArrayList<>();
	
	private Teams teams; 
	private LeagueTables leagueTables;
	
	private List<String> featureNames = new ArrayList<>();
    private List<String> columnNames;

    public Matches(LeagueTables leagueTables) {
		this.leagueTables = leagueTables;
	}
	
	public void computeFeatures(List<FeatureComputer> featureComputers) {
		for (FeatureComputer f: featureComputers) {
			featureNames.addAll(f.getColumnNames());
		}
		for (Match m: matches) {
            for (FeatureComputer f : featureComputers) {
                List<String> featureValues = f.getValues(m, this, teams, leagueTables);
                m.addFeatureValues(featureValues);
            }
            leagueTables.updateWithMatch(m);
        }
	}

    public void loadFromFile(String file) throws ParseException {
        CSV csv = CSV
                .separator(';')  // delimiter of fields
                .quote('"')      // quote character
                .create();       // new instance is immutable

        columnNames = new ArrayList<>();
        csv.read(file, new CSVReadProc() {
            public void procRow(int rowIndex, String... values) {
                if (columnNames.isEmpty()) {
                    Collections.addAll(columnNames, values);
                } else {
                    assert values.length == columnNames.size();
                    Map<String, String> asMap = asMap(values);

		    if (asMap.get("r_goals_before_penalties_home").length() == 0 || asMap.get("r_goals_before_penalties_away").length() == 0) {
			// A small hack to work around the problem, that we don't have results for the championship 2014 (yet)
		        asMap.put("r_goals_before_penalties_home","0");
			asMap.put("r_goals_before_penalties_away","0");
		        asMap.put("r_goals_before_penalties_home","0");
			asMap.put("r_goals_before_penalties_away","0");
		    }
		    try {
			matches.add(new Match(values, asMap));
		    } catch (ParseException e) {
			throw new RuntimeException(e);
		    }
                }
            }
        });
        sort(matches);
        teams = new Teams(this);
    }

    private Map<String, String> asMap(String[] values) {
        Map<String, String> asMap = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            String name = columnNames.get(i);
            asMap.put(name, value);
        }
        return asMap;
    }

    public void writeToFile(String file) {
		CSV csv = CSV
			    .separator(';')  // delimiter of fields
			    .noQuote()
			    .create();       // new instance is immutable
        csv.write(file, new CSVWriteProc() {
            public void process(CSVWriter out) {
                printColumnNames(out);
                for (Match m : matches) {
                    List<String> cells = new ArrayList<>();
                    Collections.addAll(cells, m.getOriginalValues());
                    cells.addAll(m.getExtraValues());
                    out.writeNext(cells.toArray(new String[cells.size()]));
                }
            }
        });
    }

    private void printColumnNames(CSVWriter out) {
        List<String> names = new ArrayList<>(columnNames);
        names.addAll(featureNames);
        out.writeNext(names.toArray(new String[names.size()]));
    }


    private static void sort (List<Match> toSort) {
		Collections.sort(toSort, new Comparator<Match>() {

			@Override
			public int compare(Match o1, Match o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
			
		});		
	}


    @Override
    public Iterator<Match> iterator() {
        return matches.iterator();
    }


}
