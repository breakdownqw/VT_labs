public class Shorty extends Creature implements Behavior {
    public Shorty(String name) {
        super(name);
        addState(State.HIDDEN);
    }
    @Override
    public void setState(State newState) {
        StateManager.resolveConflicts(states, newState);
    }
    @Override
    public void act(){
        if (states.contains(State.HIDDEN)) {
            System.out.println(name + " скрылся за деревом.");
        }
        if (states.contains(State.VISIBLE)) {
            System.out.println(name + " медленно подходит к Скуперфильду.");
        }
    }
    public void hide() {

        if (states.contains(State.VISIBLE)) {
            setState(State.HIDDEN);
        }
    }
    public boolean isVisible() {
        return states.contains(State.VISIBLE);
    }
    public boolean isHidden() {
        return states.contains(State.HIDDEN);
    }

}

