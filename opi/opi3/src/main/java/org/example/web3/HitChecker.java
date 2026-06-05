package org.example.web3;

public class HitChecker {

    public static boolean isHit(double x, double y, double r) {
        if (r <= 0) throw new IllegalArgumentException(Messages.get("error.r.positive"));

        // IV квадрант — прямоугольник
        if (x >= 0 && x <= r / 2 && y <= 0 && y >= -r) {
            return true;
        }

        // I квадрант — четверть круга
        if (x >= 0 && y >= 0 && (x * x + y * y) <= (r / 2) * (r / 2)) {
            return true;
        }

        // II квадрант — треугольник
        if (x <= 0 && x >= -r / 2 && y >= 0 && y <= 2 * (x + r / 2)) {
            return true;
        }

        // Вне области
        return false;
    }
}
