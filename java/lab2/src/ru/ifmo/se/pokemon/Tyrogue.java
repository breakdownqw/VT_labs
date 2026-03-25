package ru.ifmo.se.pokemon;

public class Tyrogue extends Pokemon {
    public Tyrogue(String name, int level) {
        super(name, level);
        setStats(35.0D, 35.0D, 35.0D, 35.0D, 35.0D, 35.0D);
        setType(new Type[] { Type.FIGHTING });
        setMove(new Move[] { (Move)new RockSlide(), (Move)new WorkUp(), (Move)new BulkUp() });
    }
}