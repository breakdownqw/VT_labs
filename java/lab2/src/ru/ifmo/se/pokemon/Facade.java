package ru.ifmo.se.pokemon;

class Facade extends PhysicalMove {
    protected Facade() {
        super(Type.NORMAL, 70.0D, 100.0D);
    }
    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        Status PokCon = def.getCondition();
        if (PokCon.equals(Status.BURN) || PokCon.equals(Status.POISON) || PokCon.equals(Status.PARALYZE)) {
            def.setMod(Stat.HP, (int) Math.round(damage) * 2);
        } else {
            def.setMod(Stat.HP, (int) Math.round(damage));
        }
    }
    @Override
    protected String describe() {
        return "Использует способность Facade";
    }
}