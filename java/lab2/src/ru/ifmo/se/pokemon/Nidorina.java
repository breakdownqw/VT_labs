package ru.ifmo.se.pokemon;

public class Nidorina extends Nidoran_F {
    public Nidorina(String name, int level) {
        super(name, level);
        setStats(70.0D, 62.0D, 67.0D, 55.0D, 55.0D, 56.0D);
        setType(new Type[] { Type.POISON });
        setMove(new Move[] { (Move)new Swagger(), (Move)new AerialAce(), (Move)new Flatter()});
    }
}
