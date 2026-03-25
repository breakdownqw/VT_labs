package commands;

import city.City;
import data.CityData;
import data.CityDataConverter;
import data.DataHandlerFactory;
import data.DataWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import storage.CityManager;

public class SaveCommand implements Command {
  private final CityManager cityManager;
  private final String defaultFileName;

  public SaveCommand(CityManager cityManager, String defaultFileName) {
    this.cityManager = cityManager;
    this.defaultFileName = defaultFileName;
  }

  @Override
  public void execute(String[] args) {
    if (args.length < 2) {
      System.out.println("Ошибка: укажите формат файла (xml или json).");
      return;
    }

    String format = args[1].toLowerCase();
    if (!format.equals("xml") && !format.equals("json")) {
      System.out.println("Ошибка: неподдерживаемый формат файла. Используйте 'xml' или 'json'.");
      return;
    }

    try {
      List<City> cities = cityManager.getAllCities();
      List<CityData> cityDataList = CityDataConverter.toCityDataList(cities);

      String fileName;
      if (args.length > 2) {

        fileName = ensureFileExtension(args[2], format);
      } else {

        fileName = generateFileName(format);
      }

      if (!checkFileWritable(fileName)) {
        System.out.println("Ошибка: невозможно записать в файл " + fileName);
        return;
      }

      DataWriter writer = DataHandlerFactory.getWriter(fileName);
      writer.writeDataToFile(fileName, cityDataList);
      System.out.println("Коллекция успешно сохранена в файл: " + fileName);
    } catch (Exception e) {
      System.out.println("Ошибка при сохранении коллекции: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private String generateFileName(String format) {
    int dotIndex = defaultFileName.lastIndexOf('.');
    String baseName = (dotIndex == -1) ? defaultFileName : defaultFileName.substring(0, dotIndex);
    return baseName + "." + format;
  }

  private String ensureFileExtension(String fileName, String format) {
    if (!fileName.endsWith("." + format)) {
      if (fileName.contains(".")) {
        return fileName.substring(0, fileName.lastIndexOf('.')) + "." + format;
      }
      return fileName + "." + format;
    }
    return fileName;
  }

  private boolean checkFileWritable(String fileName) {
    File file = new File(fileName);
    if (file.exists()) {
      return file.canWrite();
    } else {
      // Проверка можно ли создать файл
      try {
        return file.createNewFile() && file.delete();
      } catch (IOException e) {
        return false;
      }
    }
  }

  @Override
  public String getDescription() {
    return "сохранить коллекцию в файл (xml или json). можно использовать полный путь до файла  и явно указывать его имя";
  }
}
