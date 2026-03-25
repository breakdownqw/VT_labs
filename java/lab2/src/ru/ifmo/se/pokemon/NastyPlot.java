package ru.ifmo.se.pokemon;

class NastyPlot extends StatusMove {
    protected NastyPlot(){
        super(Type.DARK, 0.0D, 100.0D);
    }
    @Override
    protected void applyOppEffects(Pokemon p){
        p.setMod(Stat.SPECIAL_ATTACK, 2);
    }
    @Override
    protected String describe() {
        return "Использует способность Nasty Plot";
    }
}
