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
import storage.CityManager;

public class ReplaceIfLoweCommand implements Command, ScriptAwareCommand {
  private final CityManager cityManager;
  private final CityComparator cityComparator;
  private DataReader activeReader;
  private CityInputStrategy inputStrategy;
  private final ConsoleDataReader consoleReader;
  private FileDataReader fileReader;

  public ReplaceIfLoweCommand(
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
    if (args.length < 2) {
      System.out.println("Ошибка: id города не указан.");
      return;
    }

    try {
      int id = Integer.parseInt(args[1]);

      if (!cityManager.getCollection().containsKey(id)) {
        System.out.println("Город с id " + id + " не найден.");
        return;
      }

      City newCity = inputStrategy.inputObject();
      City oldCity = cityManager.getCollection().get(id);

      if (cityComparator.compare(newCity, oldCity) < 0) {
        newCity.setId(id);
        cityManager.updateCity(id, newCity);
        System.out.println("Город с id " + id + " успешно заменен.");
      } else {
        System.out.println("Новое значение не меньше старого. Замена не выполнена.");
      }
    } catch (NumberFormatException e) {
      System.out.println("Ошибка: id должен быть целым числом.");
    } catch (IllegalArgumentException e) {
      System.out.println("Ошибка: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Неожиданная ошибка: " + e.getMessage());
      setScriptMode(false, null);
    }
  }

  @Override
  public String getDescription() {
    return "заменить значение по ключу, если новое значение меньше старого";
  }

  @Override
  public void close() throws IOException {
    if (fileReader != null) {
      fileReader.close();
    }
  }
}