package commands;

import city.City;
import data.ConsoleDataReader;
import data.DataReader;
import data.FileDataReader;
import io.CityInputStrategy;
import storage.CityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class InsertCommand implements Command, ScriptAwareCommand {
  private final CityManager cityManager;
  private DataReader activeReader;
  private CityInputStrategy inputStrategy;
  private final ConsoleDataReader consoleReader;
  private FileDataReader fileReader;

  public InsertCommand(CityManager cityManager, Scanner consoleScanner) {
    this.cityManager = cityManager;
    this.consoleReader = new ConsoleDataReader(consoleScanner);
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
        this.activeReader = consoleReader; // Fallback на консоль
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
      if (city != null) {
        cityManager.addCity(city);
        System.out.println("Элемент успешно добавлен.");
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
    return "добавить новый элемент с заданным ключом";
  }

  @Override
  public void close() throws IOException {
    if (fileReader != null) {
      fileReader.close();
    }
  }
}

