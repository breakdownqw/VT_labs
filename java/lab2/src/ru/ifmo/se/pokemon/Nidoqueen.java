package ru.ifmo.se.pokemon;

public class Nidoqueen extends Nidorina {
    public Nidoqueen(String name, int level) {
        super(name, level);
        setStats(90.0D, 92.0D, 87.0D, 75.0D, 85.0D, 76.0D);
        setType(new Type[] { Type.POISON, Type.GROUND });
        setMove(new Move[] { (Move)new Swagger(), (Move)new AerialAce(), (Move)new Flatter(),(Move)new DoubleKick()});
    }
}
