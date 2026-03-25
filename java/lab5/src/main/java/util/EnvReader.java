package util;

import java.util.Scanner;

public final class EnvReader {
  private static final String DEFAULT_ENV_NAME = "collection";

  /**
   * Получает путь к файлу из переменной окружения, взаимодействуя с пользователем.
   *
   * @return путь к файлу.
   * @throws IllegalStateException если переменная окружения не установлена и пользователь не ввел
   *     значение.
   */
  public static String getFilePath() {
    Scanner scanner = new Scanner(System.in);
    String currentEnv = System.getenv(DEFAULT_ENV_NAME);

    if (currentEnv != null && !currentEnv.isEmpty()) {
      System.out.println(
          "Обнаружена переменная окружения " + DEFAULT_ENV_NAME + " = " + currentEnv);
      System.out.print("Хотите использовать её? (y/n): ");
      String choice = scanner.nextLine().trim().toLowerCase();

      if (choice.equals("y") || choice.equals("yes") || choice.isEmpty()) {
        return currentEnv;
      }
    }

    System.out.print("Введите путь к файлу коллекции: ");
    String userPath = scanner.nextLine().trim();

    if (userPath.isEmpty()) {
      throw new IllegalStateException("Путь к файлу не может быть пустым!");
    }

    return userPath;
  }
}
