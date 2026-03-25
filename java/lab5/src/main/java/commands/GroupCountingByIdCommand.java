package commands;

import java.util.Map;
import java.util.stream.Collectors;
import storage.CityManager;

/**
 * Команда для группировки элементов по четности ID и вывода количества элементов в каждой группе.
 *
 * <p>Эта команда группирует элементы коллекции на две группы: с четными и нечетными ID. После
 * группировки выводится количество элементов в каждой группе.
 *
 * <p>Если коллекция пуста, команда выводит сообщение о том, что группировать нечего.
 *
 * @see Command
 * @see CityManager
 */
public class GroupCountingByIdCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для группировки элементов по четности ID.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для группировки.
   */
  public GroupCountingByIdCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, группируя элементы коллекции по четности ID и выводя количество элементов в
   * каждой группе.
   *
   * <p>Метод проверяет, пуста ли коллекция. Если коллекция пуста, выводится сообщение об ошибке. В
   * противном случае элементы группируются на две группы: с четными и нечетными ID, и выводится
   * количество элементов в каждой группе.
   *
   * @param args аргументы команды (в данной команде не используются).
   */
  @Override
  public void execute(String[] args) {
    if (cityManager.getCollection().isEmpty()) {
      System.out.println("Коллекция пуста. Нечего группировать.");
      return;
    }

    Map<Boolean, Long> groupCounts =
        cityManager.getCollection().values().stream()
            .collect(
                Collectors.partitioningBy(city -> city.getId() % 2 == 0, Collectors.counting()));

    System.out.println("Четные ID: " + groupCounts.get(true));
    System.out.println("Нечетные ID: " + groupCounts.get(false));
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "сгруппировать элементы по id";
  }
}
