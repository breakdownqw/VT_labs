package ru.ifmo.se.pokemon;

class RockSlide extends PhysicalMove {
    protected RockSlide(){
        super(Type.ROCK, 75.0D, 90.0D);
    }
    @Override
    protected void applyOppEffects(Pokemon p){
        if (Math.random() <= 0.3) Effect.flinch(p);
    }
    @Override
    protected void applyOppDamage(Pokemon def, double damage) {
        def.setMod(Stat.HP, (int) Math.round(damage));
    }
    @Override
    protected String describe(){
        return "Использует способность Rock Slide";
    }
}