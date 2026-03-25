package ru.ifmo.se.pokemon;

public class Hitmonlee extends Tyrogue {
    public Hitmonlee(String name, int level) {
        super(name, level);
        setStats(50.0D, 120.0D, 53.0D, 35.0D, 110.0D, 87.0D);
        setType(new Type[] { Type.FIGHTING });
        setMove(new Move[] { (Move)new RockSlide(), (Move)new WorkUp(), (Move)new BulkUp(),(Move)new PoisonJab()});
    }
}
