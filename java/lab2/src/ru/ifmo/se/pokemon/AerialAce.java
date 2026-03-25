package ru.ifmo.se.pokemon;




class AerialAce extends PhysicalMove {
    protected AerialAce() {
        super(Type.FLYING, 60.0D, 100.0D);
    }
    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
            def.setMod(Stat.HP, (int)Math.round(damage));
    }
    @Override
    protected String describe() {
        return "Не промахивается";
    }
}
