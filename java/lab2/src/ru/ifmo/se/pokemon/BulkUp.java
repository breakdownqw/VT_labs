package ru.ifmo.se.pokemon;

class BulkUp extends StatusMove {
    protected BulkUp(){
        super(Type.FIGHTING, 0.0D, 100.0D);
    }
    @Override
    protected void applySelfEffects(Pokemon p){
        p.setMod(Stat.ATTACK, 1);
        p.setMod(Stat.DEFENSE, 1);
    }
    @Override
    protected String describe(){
        return "Использует способность Bulk Up";
    }
}