package ru.ifmo.se.pokemon;

class CalmMind extends StatusMove {
    protected CalmMind() {
        super(Type.PSYCHIC, 0.0D, 100.0D);
    }
    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPECIAL_ATTACK, 1);
        p.setMod(Stat.SPECIAL_DEFENSE, 1);
    }
    @Override
    protected String describe() {
        return "Использует способность Calm Mind";
    }
}
