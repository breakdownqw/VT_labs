package commands;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ExecuteScriptCommand implements Command {
  private final CommandHandler commandHandler;
  private final Set<String> activeScripts = new HashSet<>();

  public ExecuteScriptCommand(CommandHandler commandHandler) {
    this.commandHandler = commandHandler;
  }

  @Override
  public void execute(String[] args) {
    if (args.length < 2) {
      System.out.println("Ошибка: нужно указать файл скрипта");
      return;
    }

    String fileName = args[1];
    File scriptFile = new File(fileName).getAbsoluteFile();

    if (activeScripts.contains(scriptFile.getAbsolutePath())) {
      System.out.println("Ошибка: обнаружена рекурсия в скрипте " + fileName);
      return;
    }

    activeScripts.add(scriptFile.getAbsolutePath());

    try (BufferedReader fileReader = new BufferedReader(new FileReader(scriptFile))) {
      commandHandler.setScriptMode(true, fileReader);

      String commandLine;
      while ((commandLine = fileReader.readLine()) != null) {
        commandHandler.handleCommand(commandLine.trim());
      }

      commandHandler.setScriptMode(false, null);
    } catch (IOException e) {
      System.out.println("Ошибка при чтении файла: " + e.getMessage());
    } finally {
      activeScripts.remove(scriptFile.getAbsolutePath());
    }
  }

  @Override
  public String getDescription() {
    return "выполнить скрипт из файла";
  }
}
