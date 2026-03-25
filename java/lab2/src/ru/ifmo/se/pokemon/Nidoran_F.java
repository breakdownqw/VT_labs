package ru.ifmo.se.pokemon;

public class Nidoran_F extends Pokemon {
    public Nidoran_F(String name, int level) {
        super(name, level);
        setStats(55.0D, 47.0D, 52.0D, 40.0D, 40.0D, 41.0D);
        setType(new Type[] { Type.POISON });
        setMove(new Move[] { (Move)new Swagger(), (Move)new AerialAce() });
    }
}
