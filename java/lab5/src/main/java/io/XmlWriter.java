package io;

import city.Coordinates;
import city.Human;
import data.CityData;
import data.DataWriter;
import exceptions.FileWriteException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/** Класс для записи данных в XML-файл. */
public class XmlWriter implements DataWriter {

  @Override
  public void writeDataToFile(String fileName, List<CityData> cities) throws FileWriteException {
    cities.sort(Comparator.comparingInt(CityData::getId));
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      writer.write("<cities>\n");
      for (CityData city : cities) {
        writeCity(writer, city);
      }
      writer.write("</cities>\n");
    } catch (IOException e) {
      throw new FileWriteException("Ошибка при записи XML в файл: " + fileName, e);
    }
  }

  /**
   * Записывает город в XML.
   *
   * @param writer BufferedWriter.
   * @param city объект города.
   * @throws IOException если произошла ошибка при записи.
   */
  private void writeCity(BufferedWriter writer, CityData city) throws IOException {
    writer.write("  <city>\n");
    writeElement(writer, "id", city.getId());
    writeElement(writer, "name", city.getName());
    writeCoordinates(writer, city.getCoordinates());
    writeElement(writer, "creationDate", city.getCreationDate().toString());
    writeElement(writer, "area", city.getArea());
    writeElement(writer, "population", city.getPopulation());
    writeOptionalElement(writer, "metersAboveSeaLevel", city.getMetersAboveSeaLevel());
    writeElement(writer, "climate", city.getClimate().name());
    writeElement(writer, "government", city.getGovernment().name());
    writeElement(writer, "standardOfLiving", city.getStandardOfLiving().name());
    writeHuman(writer, city.getGovernor());
    writer.write("  </city>\n");
  }

  /**
   * Записывает элемент XML.
   *
   * @param writer BufferedWriter.
   * @param tagName имя тега.
   * @param value значение (null не допускается).
   * @throws IOException если произошла ошибка при записи.
   */
  private void writeElement(BufferedWriter writer, String tagName, Object value)
      throws IOException {
    writer.write("    <" + tagName + ">" + value + "</" + tagName + ">\n");
  }

  /**
   * Записывает элемент XML, если значение не null.
   *
   * @param writer BufferedWriter.
   * @param tagName имя тега.
   * @param value значение (null игнорируется).
   * @throws IOException если произошла ошибка при записи.
   */
  private void writeOptionalElement(BufferedWriter writer, String tagName, Object value)
      throws IOException {
    if (value != null) {
      writeElement(writer, tagName, value);
    }
  }

  /**
   * Записывает координаты в XML.
   *
   * @param writer BufferedWriter.
   * @param coordinates объект координат.
   * @throws IOException если произошла ошибка при записи.
   */
  private void writeCoordinates(BufferedWriter writer, Coordinates coordinates) throws IOException {
    writer.write("    <coordinates>\n");
    writeElement(writer, "x", coordinates.getX());
    writeElement(writer, "y", coordinates.getY());
    writer.write("    </coordinates>\n");
  }

  /**
   * Записывает данные о губернаторе в XML, если он есть.
   *
   * @param writer BufferedWriter.
   * @param human объект человека (если null, тег не пишется).
   * @throws IOException если произошла ошибка при записи.
   */
  private void writeHuman(BufferedWriter writer, Human human) throws IOException {
    if (human != null) {
      writer.write("    <governor>\n");
      writeElement(writer, "height", human.getHeight());
      writer.write("    </governor>\n");
    }
  }
}
