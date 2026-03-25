package commands;

import storage.CityManager;

/**
 * Команда для вывода всех элементов коллекции.
 *
 * <p>Эта команда выводит все элементы коллекции городов на экран. Если коллекция пуста, выводится
 * соответствующее сообщение.
 *
 * @see Command
 * @see CityManager
 */
public class ShowCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для вывода всех элементов коллекции.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для получения
   *     данных.
   */
  public ShowCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, выводя все элементы коллекции.
   *
   * <p>Метод проверяет, пуста ли коллекция. Если коллекция пуста, выводится сообщение об этом. В
   * противном случае все элементы коллекции выводятся на экран.
   *
   * @param args аргументы команды (в данной команде не используются).
   */
  @Override
  public void execute(String[] args) {
    if (cityManager.getCollectionSize() == 0) {
      System.out.println("Коллекция пуста.");
    } else {
      cityManager.getAllCities().forEach(System.out::println);
    }
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "вывести все элементы коллекции";
  }
}
