package ru.ifmo.se.pokemon;

public class Battleground {
    public static void main(String[] args) {
        Battle field = new Battle();
        field.addAlly(new Girafarig("Mike Tyson", 4));
        field.addAlly(new Nidoran_F("Apollo Creed", 2));
        field.addAlly(new Hitmonlee("Rocky Balboa", 4));
        field.addFoe(new Tyrogue("Ivan Drago", 3));
        field.addFoe(new Nidorina("Tyler Durden", 3));
        field.addFoe(new Nidoqueen("Muhammad Ali", 4));
        field.go();
    }
}
