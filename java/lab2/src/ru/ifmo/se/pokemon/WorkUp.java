package ru.ifmo.se.pokemon;

class WorkUp extends StatusMove {
    protected WorkUp(){
        super(Type.NORMAL, 0.0D, 100.0D);
    }
    @Override
    protected void applySelfEffects(Pokemon p){
        p.setMod(Stat.ATTACK, 1);
        p.setMod(Stat.SPECIAL_ATTACK, 1);
    }
    @Override
    protected String describe(){
        return "Использует способность Work Up";
    }
}