public class State {
    public final String name;
    public final String abbr;
    public final int electoralVotes;
    public final int popularVotes;

    public State(String name, String abbr, int electoralVotes, int popularVotes) {
        this.name = name;
        this.abbr = abbr;
        this.electoralVotes = electoralVotes;
        this.popularVotes = popularVotes;
    }

    public static State fromCsv(String commaSeparated) {
        return fromCsv(commaSeparated.split(","));
    }

    public static State fromCsv(String... args) {
        return new State(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }

    public String toString() {
        return abbr;
    }
}
