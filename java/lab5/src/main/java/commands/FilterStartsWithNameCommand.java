package commands;

import storage.CityManager;

/**
 * Команда для вывода элементов, имя которых начинается с заданной подстроки.
 *
 * <p>Эта команда фильтрует элементы коллекции, оставляя только те, чьи имена начинаются с указанной
 * подстроки, и выводит их на экран.
 *
 * <p>Команда принимает один аргумент — подстроку, с которой должно начинаться имя элемента. Если
 * аргумент не указан, выводится сообщение об ошибке.
 *
 * @see Command
 * @see CityManager
 */
public class FilterStartsWithNameCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для фильтрации элементов по имени.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для фильтрации.
   */
  public FilterStartsWithNameCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, фильтруя и выводя элементы, чьи имена начинаются с заданной подстроки.
   *
   * <p>Метод проверяет, что подстрока была указана в аргументах. Если подстрока отсутствует,
   * выводится сообщение об ошибке. В противном случае вызывается метод {@link
   * CityManager#filterByName(String)} для фильтрации и вывода элементов.
   *
   * @param args аргументы команды. Ожидается, что второй аргумент (args[1]) содержит подстроку.
   */
  @Override
  public void execute(String[] args) {
    if (args.length < 2) {
      System.out.println("Ошибка: введите подстроку");
      return;
    }
    String prefix = args[1];
    cityManager.filterByName(prefix);
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "вывести элементы, название которых начинается с подстроки";
  }
}
