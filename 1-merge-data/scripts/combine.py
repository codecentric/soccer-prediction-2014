import codecs
import time


class CSV:
    def __init__(self, fn):
        lines = [[y.strip() for y in x.split(';')] for x in codecs.open(fn, "r", "utf-8").readlines()]
        self.header = lines.pop(0)
        self.data = lines

    def map_column(self, column_name, map_func):
        index = self.header.index(column_name)
        assert index >= 0
        for i in self.data:
            i[index] = map_func(i[index], i)

    def line_as_map(self, i):
        result = {}
        for j in range(len(self.header)):
            name = self.header[j]
            value = i[j]
            result[name] = value
        return result

    def add_column(self, new_col_name, map_func):
        assert new_col_name not in self.header
        for i in self.data:
            v = map_func(self.line_as_map(i))
            i.append(v)
        self.header.append(new_col_name)

    def get_as_map(self, combine_func, *key_names):
        the_map = {}
        for i in self.data:
            as_map = self.line_as_map(i)
            key = tuple([as_map[x] for x in key_names])
            old = the_map.get(key, [])
            old.append(as_map)
            the_map[key] = old

        result = {}
        for key, value in the_map.items():
            if len(value) > 1:
                assert combine_func, key
                value = combine_func(key, value)
            else:
                value = value[0]
            result[key] = value

        return result

    def rename_column(self, old_name, new_name):
        assert old_name in self.header
        self.header[self.header.index(old_name)] = new_name


def fix_country(input, line):
    if input.endswith(" ."):
        input = input[:-1].strip()
    assert country_name_to_iso.has_key(input.lower()), "Unknown country %r" % input
    return country_name_to_iso[input.lower()]

def read_wm2014(fn):
    data = CSV(fn)
    return data.get_as_map(None, "b_date", "b_team_home", "b_team_away","b_tournament_group","b_tournament_name1","b_tournament_country","b_tournament_phase1")


def read_michael(fn):
    data = CSV(fn)

    def fix_date(input, line):
        assert input, line
        d = time.strptime(input, "%d.%m.%Y")
        return time.strftime("%Y-%m-%d", d)

    data.add_column("source", lambda x: "michael")
    data.map_column("datum", fix_date)
    data.map_column("team_a", fix_country)
    data.map_column("team_b", fix_country)

    data.rename_column("datum", "b_date")
    data.rename_column("uhrzeit", "b_time")
    data.rename_column("team_a", "b_team_home")
    data.rename_column("team_b", "b_team_away")
    data.rename_column("tore_gesamt_a", "r_goals_final_home")
    data.rename_column("tore_gesamt_b", "r_goals_final_away")
    data.rename_column("tore_vor_elfmeter_a", "r_goals_before_penalties_home")
    data.rename_column("tore_vor_elfmeter_b", "r_goals_before_penalties_away")
    data.rename_column("tore_elfmeter_a", "r_goals_during_penalties_home")
    data.rename_column("tore_elfmeter_b", "r_goals_during_penalties_away")
    data.rename_column("tore_halbzeit_a", "r_goals_half_time_home")
    data.rename_column("tore_halbzeit_b", "r_goals_half_time_away")
    data.rename_column("wettbewerb", "b_tournament_name1")
    data.rename_column("phase", "b_tournament_phase1")
    data.rename_column("stadion", "b_stadium_name")
    data.rename_column("besucher", "b_stadium_visitors")
    data.rename_column("schiedsrichter_name", "b_referee_name")
    data.rename_column("schiedsrichter_alter", "b_referee_age")
    data.rename_column("schiedsrichter_land", "b_referee_country")
    data.rename_column("aufstellung_a", "b_teamsetup_home")
    data.rename_column("aufstellung_b", "b_teamsetup_away")
    return data.get_as_map(None, "b_date", "b_team_home", "b_team_away")


def read_dataminingsoccer(*filenames):
    res = {}
    for fn in filenames:
        data = CSV(fn)

        def fix_date(input, line):
            if input == "match cancelled": return input
            if not input:
                print line
                return input

            t = input
            t = t.replace("Mai", "05")
            t = t.replace("Jun", "06")
            t = t.replace("Jul", "07")
            t = t.replace("-", " ")
            t = t.replace(".", " ")
            t = t.replace("  ", " ")
            t = t.split(" ")
            day, month, year = t
            if len(year) == 2:
                if int(year) < 30:
                    year = "20" + year
                else:
                    year = "19" + year
            t = " ".join([day, month, year])

            d = time.strptime(t, "%d %m %Y")
            assert d, [input, t]
            result = time.strftime("%Y-%m-%d", d)
            assert "2068" not in result, [input, t]
            return result

        def combine_games(key, values):
            stage = 'b_tournament_phase3'
            assert len(values) == 2
            if values[0][stage].endswith(" penalties"):
                pg, ng = values
            else:
                ng, pg = values
            assert ng[stage] + " penalties" == pg[stage], key
            ng['r_goals_final_home'] = pg['r_goals_before_penalties_home']
            ng['r_goals_final_away'] = pg['r_goals_before_penalties_away']
            return ng

        data.add_column("source", lambda x: "dataminingsoccer")
        data.map_column("date", fix_date)
        data.map_column("country team A", fix_country)
        data.map_column("country team B", fix_country)
        data.add_column("score A after penalties", lambda x: x["score A"])
        data.add_column("score B after penalties", lambda x: x["score B"])

        data.rename_column("date", "b_date")
        data.rename_column("country team A", "b_team_home")
        data.rename_column("country team B", "b_team_away")
        data.rename_column("score A", "r_goals_before_penalties_home")
        data.rename_column("score B", "r_goals_before_penalties_away")
        data.rename_column('score A after penalties', "r_goals_final_home")
        data.rename_column('score B after penalties', "r_goals_final_away")
        data.rename_column('country', "b_tournament_country")
        data.rename_column("which stage of the tournament", "b_tournament_phase3")

        res.update(data.get_as_map(combine_games, "b_date", "b_team_home", "b_team_away"))
    return res


def read_valentin(fn):
    data = CSV(fn)

    def extract_time(line):
        return line["date"].split(" ")[1]

    def fix_date(input, line):
        d = time.strptime(input.split(" ")[0], "%Y-%m-%d")
        return time.strftime("%Y-%m-%d", d)

    data.add_column("source", lambda x: "valentin")
    data.add_column("time", extract_time)
    data.map_column("date", fix_date)
    data.map_column("homeTeam", fix_country)
    data.map_column("awayTeam", fix_country)

    data.rename_column("date", "b_date")
    data.rename_column("homeTeam", "b_team_home")
    data.rename_column("awayTeam", "b_team_away")
    data.rename_column('homeTeamScore', "r_goals_final_home")
    data.rename_column('awayTeamScore', "r_goals_final_away")
    data.rename_column('homeWinBettingQuote', "b_bookie_quote_win_for_home")
    data.rename_column('awayWinBettingQuote', "b_bookie_quote_win_for_away")
    data.rename_column('drawBettingQuote', "b_bookie_quote_draw")
    data.rename_column('numberOfBookmarkers', "b_bookie_num_sources")
    data.rename_column("tournament", "b_tournament_name2")
    data.rename_column("tournamentStageName", "b_tournament_phase2")
    data.rename_column("extraTime/Penalties", "r_extra_time_penalties")
    return data.get_as_map(None, "b_date", "b_team_home", "b_team_away")


def read_iso_files(*file_names):
    r = {}
    for fn in file_names:
        for code, name in [line.split(";") for line in codecs.open(fn, "r", "utf-8").readlines()]:
            r[name.strip().lower()] = code.strip()
    return r


def show_conflicting_information(s1, s2, s3):
    print "Games where the data sources disagree:"
    for key in sorted(list(set(s1.keys() + s2.keys() + s3.keys()))):
        v1 = s1.get(key, None)
        v2 = s2.get(key, None)
        v3 = s3.get(key, None)

        v = [v3, v2, v1]
        v = [x for x in v if x]

        sources = [x["source"] for x in v]
        home_goals = [x["r_goals_final_home"] for x in v]
        away_goals = [x["r_goals_final_away"] for x in v]
        if len(v) > 1:
            if len(set(home_goals)) > 1 or len(set(away_goals)) > 1:
                print "   ", key,
                for i in range(len(sources)):
                    s = "%s:%s" % (home_goals[i], away_goals[i])
                    print sources[i], s,
                print


def write_combined_files(filename, s1, s2, s3, s4):
    all = {}
    for key in sorted(list(set(s1.keys() + s2.keys() + s3.keys() + s4.keys()))):
        v1 = s1.get(key, None)
        v2 = s2.get(key, None)
        v3 = s3.get(key, None)
        v4 = s4.get(key, None)

        m = {}
        # v1  has the best data quality so use it last
        v = [v4, v3, v2, v1]
        v = [x for x in v if x]
        for i in v:
            m.update(i)
        all[key] = m
    new_cols = sorted(list(set(s1.values()[0].keys() + s2.values()[0].keys() + s3.values()[0].keys() + s4.values()[0].keys())))
    new_cols.remove("b_date")
    new_cols.remove("b_team_home")
    new_cols.remove("b_team_away")
    new_cols = ["b_date", "b_team_home", "b_team_away"] + new_cols
    new_cols.remove("source")
    with codecs.open(filename, "w", "utf-8") as out:
        out.write(";".join(new_cols) + "\n")
        for key in sorted(all.keys()):
            d = [all[key].get(x, "") for x in new_cols]
            out.write(";".join(d) + "\n")


country_name_to_iso = read_iso_files(
    "input/iso_3166_alpha_3_de.csv", 
    "input/iso_3166_alpha_3_en.csv",
    "input/iso_3166_alpha_3_de_extra.csv", 
    "input/iso_3166_alpha_3_en_extra.csv",
    "input/fifa_codes_de.csv",
    "input/fifa_codes_en.csv")

s1 = read_michael("../0-raw-sources/from-michael.csv")
s2 = read_dataminingsoccer("../0-raw-sources/european_cups.csv",
                           "../0-raw-sources/world_cups.csv")
s3 = read_valentin("../0-raw-sources/from-valentin.csv")
s4 = read_wm2014("../0-raw-sources/wm-2014.csv")

show_conflicting_information(s1, s2, s3)
write_combined_files("output/games.csv", s1, s2, s3, s4)
