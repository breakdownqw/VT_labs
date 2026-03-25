package commands;

import java.util.List;
import storage.CityManager;

/**
 * Команда для удаления элементов, ключ которых превышает заданный.
 *
 * <p>Эта команда удаляет все элементы коллекции, ключи которых больше указанного значения. Ключ
 * должен быть целым числом, иначе выводится сообщение об ошибке.
 *
 * <p>Если коллекция пуста, выводится сообщение о том, что удалять нечего. Если ни один элемент не
 * имеет ключа больше заданного, выводится соответствующее сообщение. В противном случае выводится
 * количество удаленных элементов.
 *
 * @see Command
 * @see CityManager
 */
public class RemoveGreaterKeyCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для удаления элементов с ключами больше заданного.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для удаления
   *     элементов.
   */
  public RemoveGreaterKeyCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, удаляя элементы с ключами больше заданного.
   *
   * <p>Метод проверяет, был ли передан ключ в качестве аргумента. Если ключ не указан или имеет
   * неверный формат (не является целым числом), выводится сообщение об ошибке. Если коллекция
   * пуста, выводится сообщение о том, что удалять нечего. В противном случае удаляются все
   * элементы, ключи которых больше заданного, и выводится количество удаленных элементов.
   *
   * @param args аргументы команды. Ожидается, что второй аргумент (args[1]) содержит ключ.
   */
  @Override
  public void execute(String[] args) {
    if (args.length < 2) {
      System.out.println("Ошибка: необходимо указать ключ.");
      return;
    }

    try {
      int key = Integer.parseInt(args[1]);

      if (cityManager.getCollection().isEmpty()) {
        System.out.println("Коллекция пуста. Удалять нечего.");
        return;
      }

      long initialSize = cityManager.getCollection().size();

      List<Integer> keysToRemove =
          cityManager.getCollection().keySet().stream().filter(id -> id > key).toList();

      keysToRemove.forEach(cityManager::removeCity);

      long finalSize = cityManager.getCollection().size();

      if (initialSize == finalSize) {
        System.out.println("Нет элементов с ключами больше " + key + ".");
      } else {
        System.out.println("Удалено " + (initialSize - finalSize) + " элементов.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Ошибка: ключ должен быть целым числом.");
    }
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "удалить элементы с ключами больше заданного";
  }
}
