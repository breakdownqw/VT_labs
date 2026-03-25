public record Bug(String type, Creature target) implements Behavior {
    @Override
    public void act() {
        System.out.println(type + " кусает " + target.getName() + "!");
        target.addState(State.BITTEN);
    }
}
