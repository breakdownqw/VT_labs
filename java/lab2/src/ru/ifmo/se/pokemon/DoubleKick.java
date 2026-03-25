package ru.ifmo.se.pokemon;

class DoubleKick extends PhysicalMove {
    protected DoubleKick() {
        super(Type.FIGHTING, 30.0D, 100.0D);
    }
    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
        def.setMod(Stat.HP, (int) Math.round(damage));

    }
    @Override
    protected String describe() {
        return "Использует способность Double Kick";
    }
}