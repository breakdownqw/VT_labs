package commands;

/** Интерфейс для всех команд. */
public interface Command {
  /**
   * Выполняет команду.
   *
   * @param args аргументы команды.
   */
  void execute(String[] args);

  /**
   * Возвращает описание команды.
   *
   * @return описание команды.
   */
  String getDescription();
}
