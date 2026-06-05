package org.example.web3;

/**
 * Минимальная точка входа, чтобы Ant мог собрать исполняемый JAR.
 * Основной веб-проект всё равно запускается в Jakarta EE/JSF контейнере,
 * но для лабораторной по Ant нужен Main-Class в MANIFEST.MF.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("opi3 jar started");
        System.out.println("Sample hit check: " + HitChecker.isHit(0, 0, 1));
    }
}
