package commands;

import storage.CityManager;

/**
 * Команда для очистки коллекции.
 *
 * <p>Эта команда очищает коллекцию городов, управляемую объектом {@link CityManager}. После
 * выполнения команды коллекция становится пустой.
 *
 * @see Command
 * @see CityManager
 */
public class ClearCommand implements Command {
  private final CityManager cityManager;

  /**
   * Создает команду для очистки коллекции.
   *
   * @param cityManager менеджер коллекции городов, который будет использоваться для очистки.
   */
  public ClearCommand(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  /**
   * Выполняет команду очистки коллекции.
   *
   * <p>Метод вызывает метод {@link CityManager#clearCollection()} для очистки коллекции и выводит
   * сообщение об успешном выполнении операции.
   *
   * @param args аргументы команды (в данной команде не используются).
   */
  @Override
  public void execute(String[] args) {
    cityManager.clearCollection();
    System.out.println("Коллекция успешно очищена.");
  }

  @Override
  public String getDescription() {
    return "очистить коллекцию";
  }
}
