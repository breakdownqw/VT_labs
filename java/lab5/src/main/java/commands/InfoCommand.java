package commands;

import storage.CityManager;
import util.DateUtils;

/**
 * Команда для вывода информации о коллекции.
 *
 * <p>Эта команда выводит основную информацию о коллекции, включая: - Тип коллекции. - Количество
 * элементов в коллекции. - Дату инициализации коллекции. - Дату последнего изменения коллекции.
 *
 * @see Command
 * @see CityManager
 * @see DateUtils
 */
public class InfoCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для вывода информации о коллекции.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для получения
   *     информации.
   */
  public InfoCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, выводя информацию о коллекции.
   *
   * <p>Метод выводит: - Тип коллекции. - Количество элементов в коллекции. - Дату инициализации
   * коллекции. - Дату последнего изменения коллекции.
   *
   * @param args аргументы команды (в данной команде не используются).
   */
  @Override
  public void execute(String[] args) {
    System.out.println("Тип коллекции: " + cityManager.getCollection().getClass().getSimpleName());
    System.out.println("Количество элементов: " + cityManager.getCollectionSize());
    System.out.println(
        "Дата инициализации: " + DateUtils.formatDateTime(cityManager.getInitializationTime()));
    System.out.println(
        "Дата последнего изменения: " + DateUtils.formatDateTime(cityManager.getLastUpdateTime()));
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "вывести информацию о коллекции";
  }
}
