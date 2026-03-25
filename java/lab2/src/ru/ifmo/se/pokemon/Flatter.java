package ru.ifmo.se.pokemon;

class Flatter extends StatusMove {
    protected Flatter(){
        super(Type.DARK, 0.0D, 100.0D);
    }
    @Override
    protected void applyOppEffects(Pokemon p){
        p.setMod(Stat.SPECIAL_ATTACK, 1);
        Effect.confuse(p);
    }
    @Override
    protected String describe() {
        return "Использует способность Flatter";
    }
}