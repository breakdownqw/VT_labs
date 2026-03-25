package commands;

/**
 * Команда для вывода справки по доступным командам.
 *
 * <p>Эта команда выводит список всех доступных команд, зарегистрированных в {@link CommandHandler}.
 *
 * @see Command
 * @see CommandHandler
 */
public class HelpCommand implements Command {
  private final CommandHandler commandHandler;

  /**
   * Создает команду для вывода справки.
   *
   * @param commandHandler обработчик команд, содержащий карту доступных команд.
   */
  public HelpCommand(CommandHandler commandHandler) {
    this.commandHandler = commandHandler;
  }

  /**
   * Выполняет команду, выводя список доступных команд.
   *
   * <p>Метод использует карту команд из {@link CommandHandler}, чтобы вывести имена и описания всех
   * зарегистрированных команд.
   *
   * @param args аргументы команды (в данной команде не используются).
   */
  @Override
  public void execute(String[] args) {
    System.out.println("Доступные команды:");
    commandHandler
        .getCommands()
        .forEach(
            (name, command) -> {
              System.out.println(name + " - " + command.getDescription());
            });
  }

  @Override
  public String getDescription() {
    return "вывести справку по командам";
  }
}
