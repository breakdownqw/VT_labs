package commands;

import storage.CityManager;

/**
 * Команда для удаления элемента коллекции по ключу.
 *
 * <p>Эта команда удаляет элемент коллекции по указанному ключу (ID города). Ключ должен быть целым
 * числом, иначе выводится сообщение об ошибке.
 *
 * <p>Если элемент с указанным ключом найден, он удаляется из коллекции, и выводится сообщение об
 * успешном удалении. Если элемент не найден, выводится соответствующее сообщение.
 *
 * @see Command
 * @see CityManager
 */
public class RemoveKeyCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для удаления элемента по ключу.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для удаления
   *     элемента.
   */
  public RemoveKeyCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду, удаляя элемент коллекции по ключу.
   *
   * <p>Метод проверяет, был ли передан ключ (ID города) в качестве аргумента. Если ключ не указан
   * или имеет неверный формат (не является целым числом), выводится сообщение об ошибке. Если
   * элемент с указанным ключом найден, он удаляется из коллекции, и выводится сообщение об успешном
   * удалении. Если элемент не найден, выводится соответствующее сообщение.
   *
   * @param args аргументы команды. Ожидается, что второй аргумент (args[1]) содержит ключ (ID
   *     города).
   */
  @Override
  public void execute(String[] args) {
    if (args.length < 2) {
      System.out.println("Ошибка: необходимо передать id города.");
      return;
    }

    try {
      int id = Integer.parseInt(args[1]);
      if (cityManager.getCollection().containsKey(id)) {
        cityManager.removeCity(id);
        System.out.println("Город с id " + id + " успешно удален.");
      } else {
        System.out.println("Город с id " + id + " не найден.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Ошибка: id должен быть целым числом.");
    }
  }

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  @Override
  public String getDescription() {
    return "удалить элемент по ключу";
  }
}
