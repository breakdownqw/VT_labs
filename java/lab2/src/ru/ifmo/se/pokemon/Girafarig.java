package ru.ifmo.se.pokemon;

public class Girafarig extends Pokemon {
    public Girafarig(String name, int level) {
        super(name, level);
        setStats(70.0D, 80.0D, 65.0D, 90.0D, 65.0D, 85.0D);
        setType(new Type[] { Type.NORMAL,Type.PSYCHIC });
        setMove(new Move[] { (Move)new CalmMind(), (Move)new DoubleHit(), (Move)new Facade(), (Move)new NastyPlot() });
    }
}