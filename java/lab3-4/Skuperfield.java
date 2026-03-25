import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Skuperfield extends Creature implements Behavior {

    private static final Random random = new Random();

    public Skuperfield() {
        super("Скуперфильд");
    }
    @Override
    public void setState(State newState) {
        if (states.size() == 1) {
            states.clear();
            states.add(newState);
        } else {
            StateManager.resolveConflicts(states, newState);
        }
    }
    @Override
    public void act() {
        if (getStates().contains(State.BITTEN)) {
            System.out.println("Чувствуя себя не в силах расправиться с ничтожным насекомым, "
                    + name + " пришел в бешенство.");
            addState(State.ANGRY);
        }
    }

    public void observeShorties(List<Shorty> shorties) {

        List<Shorty> visibleShorties = getVisibleShorties(shorties);


        printForestStatus(shorties, visibleShorties);


        Optional<Shorty> recognized = tryToRecognize(shorties);
        recognized.ifPresentOrElse(
                shorty -> {
                    System.out.println("Один из них показался ему похожим на " + shorty.getName() + ".");
                    shorty.hide();
                },
                () -> System.out.println("Но Скуперфильд никого не узнал.")
        );


        if (random.nextInt(100) < 20) {
            System.out.println("При виде Скуперфильда все коротышки испугались и спрятались!");
            shorties.forEach(Shorty::hide);
        }
    }

    private List<Shorty> getVisibleShorties(List<Shorty> shorties) {
        return shorties.stream()
                .filter(shorty -> shorty.getStates().contains(State.VISIBLE))
                .toList();
    }

    private void printForestStatus(List<Shorty> shorties, List<Shorty> visibleShorties) {
        if (shorties.isEmpty()) {
            System.out.println("Скуперфильд поднял голову на полностью пустой лес.");
        } else if (visibleShorties.isEmpty()) {
            System.out.println("Скуперфильд поднял голову, но вокруг никого не было видно.");
        } else {
            System.out.println("Скуперфильд поднял голову и увидел вдали " + visibleShorties.size() + " коротышек.");
        }
    }

    private Optional<Shorty> tryToRecognize(List<Shorty> shorties) {

        return random.nextBoolean() ? Optional.of(shorties.get(random.nextInt(shorties.size()))) : Optional.empty();
    }
}

