import java.util.*;

abstract class Creature {
    protected String name;
    protected Set<State> states;


    private static final Set<String> existingNames = new HashSet<>();

    public Creature(String name) {
        if (existingNames.contains(name)) {
            throw new IllegalArgumentException("Персонаж с таким именем уже существует!");
        }
        this.name = name;
        this.states = new HashSet<>();
        existingNames.add(name);
    }
    public static class StateManager {
        private static final Map<State, List<State>> conflictMap = new HashMap<>();

        static {
            conflictMap.put(State.HIDDEN, List.of(State.VISIBLE));
            conflictMap.put(State.VISIBLE, List.of(State.HIDDEN));
            conflictMap.put(State.CALM, List.of(State.ANGRY));
            conflictMap.put(State.ANGRY, List.of(State.CALM));
        }

        public static void resolveConflicts(Set<State> states, State newState) {
            List<State> conflictingStates = conflictMap.getOrDefault(newState, List.of());
            states.removeAll(conflictingStates);
            states.add(newState);
        }
    }

    public void addState(State newState) {
        StateManager.resolveConflicts(states, newState);
    }

    public void removeState(State stateToRemove) {
        states.remove(stateToRemove);
    }

    public abstract void setState(State newState);

    public String getName() {
        return name;
    }

    public Set<State> getStates() {
        return states;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Creature creature = (Creature) o;
        return Objects.equals(name, creature.name) && Objects.equals(states, creature.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, states);
    }

    @Override
    public String toString() {
        return "Creature{name='" + name + "', states=" + states + '}';
    }
}

