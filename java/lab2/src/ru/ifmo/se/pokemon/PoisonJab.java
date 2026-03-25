package ru.ifmo.se.pokemon;

class PoisonJab extends PhysicalMove {
    protected PoisonJab(){
        super(Type.POISON, 80.0D, 100.0D);
    }
    @Override
    protected void applyOppEffects(Pokemon p){
        if (Math.random() <= 0.3) Effect.poison(p);
    }
    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
    }
    @Override
    protected String describe(){
        return "Использует способность Poison Jab";
    }
}
