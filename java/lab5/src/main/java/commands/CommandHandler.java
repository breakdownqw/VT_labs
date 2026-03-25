package commands;

import city.CityComparator;
import io.XmlWriter;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import storage.CityManager;

/** Обработчик команд. */
public class CommandHandler {
  private final Map<String, Command> commands = new HashMap<>();
  private final Scanner scanner;
  private boolean isScriptMode = false;
  private BufferedReader scriptReader;

  public CommandHandler(
      CityManager cityManager,
      Scanner scanner,
      CityComparator cityComparator,
      XmlWriter xmlWriter,
      String filename) {
    this.scanner = scanner;

    commands.put("help", new HelpCommand(this));
    commands.put("info", new InfoCommand(cityManager));
    commands.put("show", new ShowCommand(cityManager));
    commands.put("insert", new InsertCommand(cityManager, scanner));
    commands.put("update", new UpdateCommand(cityManager, scanner));
    commands.put("remove_key", new RemoveKeyCommand(cityManager));
    commands.put("clear", new ClearCommand(cityManager));
    commands.put("save", new SaveCommand(cityManager, filename));
    commands.put("execute_script", new ExecuteScriptCommand(this));
    commands.put("exit", new ExitCommand());
    commands.put("remove_greater", new RemoveGreaterCommand(cityManager, scanner, cityComparator));
    commands.put("replace_if_lowe", new ReplaceIfLoweCommand(cityManager, scanner, cityComparator));
    commands.put("remove_greater_key", new RemoveGreaterKeyCommand(cityManager));
    commands.put("group_counting_by_id", new GroupCountingByIdCommand(cityManager));
    commands.put("filter_starts_with_name", new FilterStartsWithNameCommand(cityManager));
    commands.put("print_ascending", new PrintAscendingCommand(cityManager));
  }

  /**
   * Возвращает карту зарегистрированных команд.
   *
   * @return карта команд, где ключ — имя команды, значение — объект команды.
   */
  public Map<String, Command> getCommands() {
    return commands;
  }

  public void setScriptMode(boolean isScriptMode, BufferedReader scriptReader) {
    this.isScriptMode = isScriptMode;
    this.scriptReader = scriptReader;
  }

  public void handleCommand(String input) {
    String[] parts = input.trim().split("\\s+");
    if (parts.length == 0) return;
    String commandName = parts[0];
    Command command = commands.get(commandName);

    if (command != null) {
      if (command instanceof ScriptAwareCommand) {
        ((ScriptAwareCommand) command).setScriptMode(isScriptMode, scriptReader);
      }
      command.execute(parts);
    } else {
      System.out.println("Неизвестная команда. Введите help для справки.");
    }
  }
}
