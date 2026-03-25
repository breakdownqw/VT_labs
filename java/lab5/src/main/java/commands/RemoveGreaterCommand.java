package commands;

import city.City;
import city.CityComparator;
import data.ConsoleDataReader;
import data.DataReader;
import data.FileDataReader;
import io.CityInputStrategy;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.Predicate;
import storage.CityManager;

public class RemoveGreaterCommand implements Command, ScriptAwareCommand {
  private final CityManager cityManager;
  private final CityComparator cityComparator;
  private DataReader activeReader;
  private CityInputStrategy inputStrategy;
  private final ConsoleDataReader consoleReader;
  private FileDataReader fileReader;

  public RemoveGreaterCommand(
          CityManager cityManager, Scanner scanner, CityComparator cityComparator) {
    this.cityManager = cityManager;
    this.cityComparator = cityComparator;
    this.consoleReader = new ConsoleDataReader(scanner);
    this.activeReader = consoleReader;
    this.inputStrategy = new CityInputStrategy(consoleReader);
  }

  @Override
  public void setScriptMode(boolean isScriptMode, BufferedReader scriptReader) {
    if (isScriptMode) {
      try {
        if (fileReader != null) {
          fileReader.close();
        }
        this.fileReader = new FileDataReader(scriptReader);
        this.activeReader = fileReader;
      } catch (IOException e) {
        System.err.println("Ошибка при переключении в режим скрипта: " + e.getMessage());
        this.activeReader = consoleReader;
      }
    } else {
      this.activeReader = consoleReader;
    }
    this.inputStrategy = new CityInputStrategy(activeReader);
  }

  @Override
  public void execute(String[] args) {
    try {
      City city = inputStrategy.inputObject();

      if (cityManager.getCollection().isEmpty()) {
        System.out.println("Коллекция пуста. Удалять нечего.");
        return;
      }

      Predicate<City> condition = c -> cityComparator.compare(c, city) > 0;
      int removedCount = cityManager.removeIf(condition);

      if (removedCount == 0) {
        System.out.println("Нет элементов, превышающих заданный.");
      } else {
        System.out.println("Удалено " + removedCount + " элементов.");
      }
    } catch (IllegalArgumentException e) {
      System.out.println("Ошибка: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Неожиданная ошибка: " + e.getMessage());
      setScriptMode(false, null);
    }
  }

  @Override
  public String getDescription() {
    return "удалить элементы, превышающие заданный";
  }

  @Override
  public void close() throws IOException {
    if (fileReader != null) {
      fileReader.close();
    }
  }
}
