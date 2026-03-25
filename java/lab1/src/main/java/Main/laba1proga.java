package Main;

public class laba1proga {

    public static void main(String[] args) {

        int size = (17 - 5) / 2 + 1;
        int[] r = new int[size];

        fillOddNumbers(r);
        
        

        double[] x = new double[20];
        fillRandomNumbers(x);

        double[][] r1 = new double[7][20];
        fillMatrix(r1, r, x);
        printMatrix(r1);
    }

    private static void fillMatrix(double[][] r1, int[] r, double[] x) {
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < x.length; j++) {
                r1[i][j] = calculateElement(r[i], x[j]);
            }
        }
    }

    private static void fillOddNumbers(int[] r) {
        int index = 0;
        for (int i = 17; i >= 5; i -= 2) {
            r[index++] = i;
        }
    }

    private static void fillRandomNumbers(double[] x) {
        for (int i = 0; i < x.length; i++) {
            x[i] = -11.0 + (Math.random() * 19.0); 
        }
    }

    private static double calculateElement(int rValue, double xValue) {
        if (rValue == 9) {
            return Math.pow((2.0 / Math.pow(Math.atan((xValue - 1.5) / 19.0), 1.0 / 3.0)), Math.atan(Math.exp(-Math.abs(xValue))));
        } else if (rValue == 7 || rValue == 15 || rValue == 17) {
            return Math.sin(1.0 / 4.0 / (1.0 / 2.0 + Math.exp(xValue)));
        } else {
            return Math.asin(Math.sin(Math.pow((0.5 / Math.tan(xValue) + 1.0 / 2.0), 3)));
        }
    }

    private static void printMatrix(double[][] matrix) {
    
        System.out.printf("%-10s", "   "); // Пустое место для верхнего угла
        for (int j = 0; j < matrix[0].length; j++) {
            System.out.printf("%-10s", "x" + (j + 1)); // Заголовки столбцов
        }
        System.out.println();
       
        
        
        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("r%d: ", i + 1); // Заголовки строк
            for (double value : matrix[i]) {
                System.out.printf("| %-7.4f ", value); 
            }
            System.out.println("|");
            
        }
    }
}