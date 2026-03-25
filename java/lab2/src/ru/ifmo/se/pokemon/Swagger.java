package ru.ifmo.se.pokemon;

class Swagger extends StatusMove {
    protected Swagger(){
        super(Type.NORMAL, 0.0D, 90.0D);
    }
    @Override
    protected void applyOppEffects(Pokemon p){
        p.setMod(Stat.ATTACK, 2);
        Effect.confuse(p);
    }
    @Override
    protected String describe() {
        return "Использует способность Swagger";
    }
}
