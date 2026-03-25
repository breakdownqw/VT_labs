import java.util.*;

public class story {
    public static void main(String[] args) {

        Skuperfield skuperfield = new Skuperfield();
        skuperfield.addState(State.CALM);


        List<Shorty> shorties = new ArrayList<>();
        shorties.add(new Shorty("Крабс"));
        shorties.add(new Shorty("Мига"));
        shorties.add(new Shorty("Жулио"));


        Random random = new Random();
        Tree tree3= new Tree("Ольха",25,"Лесу");
        Tree tree2= new Tree("Береза",30,"Лесу");
        Tree tree= new Tree("Дуб",20,"Лесу");
        Bug bug = new Bug("Клоп",skuperfield);


        try {

            if (random.nextBoolean()) {
                bug.act();
                skuperfield.act();
            } else {
                throw new BugBiteException();
            }
        } catch (BugBiteException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        Forest.makeSoundAndRevealShorties(shorties);
        if (shorties.isEmpty()){
            throw new EnvironmentException("Коротышек не существует");
        }else{
            skuperfield.observeShorties(shorties);
            List<Shorty> hiddenShorties = shorties.stream()
                    .filter(Shorty::isHidden)
                    .toList();
            if (hiddenShorties.size()==1){
                tree.hideShorty(hiddenShorties.getFirst().getName());
            }
        }
        if (shorties.stream().allMatch(shorty -> shorty.getStates().contains(State.HIDDEN))){
            List<Shorty> hiddenShorties = shorties.stream()
                    .filter(Shorty::isHidden)
                    .toList();
            tree.hideShorty(hiddenShorties.getFirst().getName());
            tree2.hideShorty(hiddenShorties.get(1).getName());
            tree3.hideShorty(hiddenShorties.get(2).getName());
            throw new EnvironmentException("Все коротышки спрятались. Сценарий завершен неожиданно!");
        }else{
            List<Shorty> visibleShorties = shorties.stream()
                    .filter(Shorty::isVisible)
                    .toList();
            int visibleCount = visibleShorties.size();
            if (visibleCount == 3) {
                System.out.println("Трое подошли к Скуперфильду");
                System.out.print("Читатель, безусловно, уже догадался, что это были ");
            } else if (visibleCount == 2) {
                System.out.println("Двое других тем временем подошли к Скуперфильду");
                System.out.print("Читатель, безусловно, уже догадался, что эти двое были ");
            } else {
                System.out.println(visibleCount + " коротышек подошли к Скуперфильду");
                System.out.print("Читатель, безусловно, уже догадался, что это были ");
            }


            for (int i = 0; i < visibleShorties.size(); i++) {
                System.out.print(visibleShorties.get(i).getName());
                if (i < visibleShorties.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }
}

