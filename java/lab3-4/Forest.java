import java.util.List;

public class Forest {
    public static void makeSoundAndRevealShorties(List<Shorty> shorties) {
        if (shorties == null || shorties.isEmpty()){
            System.out.println("Лес пустует");
        }else {
            System.out.println("В лесу послышались чьи-то шаги.");
            shorties.forEach(shorty -> shorty.setState(State.VISIBLE));
        }
    }
}
