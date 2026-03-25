package ru.ifmo.se.pokemon;

class DoubleHit extends PhysicalMove {
    protected DoubleHit() {
        super(Type.NORMAL, 35.0D, 90.0D);
    }
    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
        def.setMod(Stat.HP, (int) Math.round(damage));

    }
    @Override
    protected String describe() {
        return "Использует способность Double Hit";
    }
}