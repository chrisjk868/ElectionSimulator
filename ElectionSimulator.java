import java.util.*;
import java.io.*;
// Christopher Ku
// Section: AG with Jiamae Wang
// Assessment 6: ElectionSimulator
//
// The ElectionSimulator class represents the electoral and popular
// votes of each state within the United States. It could be used to
// to calculate the the minimum number of popular votes needed to win
// the Electoral college given a specific year and unique data gathered
// from each state.
public class ElectionSimulator {
    private Map<Arguments, Set<State>> combinations;
    private List<State> statesInfo;
    private int minElecVotes;

    /**
     * Takes in a parameter called states of a datatype of List<State> and
     * constructs an ElectionSimulator instance that conveys information for
     * each state.
     *
     * @param states   A List<State> parameter that contains a list of states
     *                 and their respective information for a specific year.
     */
    public ElectionSimulator(List<State> states) {
        this.combinations = new HashMap<>();
        this.statesInfo = states;
        this.minElecVotes = this.minElectoralVotes(states);
    }

    /**
     * Initalizes the evaluation of each state to process and return
     * a Set of states that represents the lowest number of popular 
     * votes needed to win the electoral college.
     *
     * @return   returns a set of states that represents the lowest number of
     *           popular votes needed to win the electoral college in the US 
     *           for a specific year.
     */
    public Set<State> simulate() {
        return simulate(this.minElecVotes, 0);
    }

    /**
     * Takes in 2 parameters one inteer representing the minimum electoral votes needed
     * to win the electoral college for a specific year, and an integer representing
     * the current state of interest or inspection within the List of states provided
     * to the ElectionSimulator instance. Afterwards starts to evaluate and construct
     * a list of states that represents states with the minimum number of popular votes
     * needed to win the electoral college. At the end it returns this list of states.
     *
     * @param electoralVotes   An integer representing the minimum electoral votes needed
     *                         to win the electoral college for a specific year.
     *
     * @param index   an integer representing the current state of interest or inspection
     *                within the List of states provided to the ElectionSimulator instance.
     *
     * @return   A list of states that represents states with the minimum number of popular votes
     *           needed to win the electoral college in the US for specific year.
     */
    private Set<State> simulate(int electoralVotes, int index) {
        Arguments tuple = new Arguments(electoralVotes, index);
        if (combinations.containsKey(tuple)) {
            return combinations.get(tuple);
        } else if (electoralVotes <= 0) {
            return new HashSet<>();
        } else if (index > statesInfo.size() - 1) {
            return null;
        } else {
            Set<State> with = simulate(electoralVotes - statesInfo.get(index).electoralVotes, index + 1);
            if (with == null) {
                return null;
            }
            Set<State> save = new HashSet<>(with);
            save.add(statesInfo.get(index));
            Set<State> without = simulate(electoralVotes, index + 1);
            if (without != null && minPopularVotes(without) < minPopularVotes(save)) {
                return without;
            }
            combinations.put(tuple, save);
            return save;
        }
    }

    public static int minElectoralVotes(List<State> states) {
        int total = 0;
        for (State state : states) {
            total += state.electoralVotes;
        }
        return total / 2 + 1;
    }

    public static int minPopularVotes(Set<State> states) {
        int total = 0;
        for (State state : states) {
            total += state.popularVotes / 2 + 1;
        }
        return total;
    }

    private static class Arguments implements Comparable<Arguments> {
        public final int electoralVotes;
        public final int index;

        public Arguments(int electoralVotes, int index) {
            this.electoralVotes = electoralVotes;
            this.index = index;
        }

        public int compareTo(Arguments other) {
            int cmp = Integer.compare(this.electoralVotes, other.electoralVotes);
            if (cmp == 0) {
                cmp = Integer.compare(this.index, other.index);
            }
            return cmp;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof Arguments)) {
                return false;
            }
            Arguments other = (Arguments) o;
            return this.electoralVotes == other.electoralVotes && this.index == other.index;
        }

        public int hashCode() {
            return Objects.hash(electoralVotes, index);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<State> states = new ArrayList<>(51);
        try (Scanner input = new Scanner(new File("data/1828.csv"))) {
            while (input.hasNextLine()) {
                states.add(State.fromCsv(input.nextLine()));
            }
        }
        Set<State> result = new ElectionSimulator(states).simulate();
        System.out.println(result);
        System.out.println(minPopularVotes(result) + " votes");
    }
}
