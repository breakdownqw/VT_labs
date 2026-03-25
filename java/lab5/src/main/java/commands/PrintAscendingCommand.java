package commands;

import city.City;
import city.CityComparator;
import java.util.List;
import storage.CityManager;

/**
 * Команда для вывода элементов коллекции в порядке возрастания.
 *
 * <p>Эта команда сортирует элементы коллекции с использованием {@link CityComparator} и выводит их
 * на экран в порядке возрастания.
 *
 * <p>Если коллекция пуста, выводится сообщение о том, что коллекция пуста.
 *
 * @see Command
 * @see City
 * @see CityComparator
 * @see CityManager
 */
public class PrintAscendingCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для вывода элементов коллекции в порядке возрастания.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для получения и
   *     сортировки элементов.
   */
  public PrintAscendingCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, выводя элементы коллекции в порядке возрастания.
   *
   * <p>Метод проверяет, пуста ли коллекция. Если коллекция пуста, выводится сообщение об этом. В
   * противном случае элементы сортируются с использованием {@link CityComparator} и выводятся на
   * экран.
   *
   * @param args аргументы команды (в данной команде не используются).
   */
  @Override
  public void execute(String[] args) {
    if (cityManager.getAllCities().isEmpty()) {
      System.out.println("Коллекция пуста");
    } else {
      List<City> cities = cityManager.getAllCities();
      cities.sort(new CityComparator());
      cities.forEach(System.out::println);
    }
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "вывести элементы в порядке возрастания";
  }
}
