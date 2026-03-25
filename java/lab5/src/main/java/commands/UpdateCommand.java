package commands;

import city.City;
import data.ConsoleDataReader;
import data.DataReader;
import data.FileDataReader;
import io.CityInputStrategy;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import storage.CityManager;

public class UpdateCommand implements Command, ScriptAwareCommand {
  private final CityManager cityManager;
  private DataReader activeReader;
  private CityInputStrategy inputStrategy;
  private final ConsoleDataReader consoleReader;
  private FileDataReader fileReader;

  public UpdateCommand(CityManager cityManager, Scanner scanner) {
    this.cityManager = cityManager;
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
      System.out.println("Ошибка: необходимо указать ID города.");
      return;
    }

    try {
      int id = Integer.parseInt(args[1]);

      if (!cityManager.getCollection().containsKey(id)) {
        System.out.println("Город с id " + id + " не найден.");
        return;
      }

      City newCity = inputStrategy.inputObject();
      newCity.setId(id);
      cityManager.updateCity(id, newCity);
      System.out.println("Город с id " + id + " успешно обновлен.");
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
    return "обновить значение элемента коллекции по указанному id";
  }

  @Override
  public void close() throws IOException {
    if (fileReader != null) {
      fileReader.close();
    }
  }
}