package data;

import io.JsonLoader;
import io.JsonWriter;
import io.XmlReader;
import io.XmlWriter;

public class DataHandlerFactory {
  public static DataLoader getLoader(String fileName) {
    if (fileName.endsWith(".json")) {
      return new JsonLoader();
    } else if (fileName.endsWith(".xml")) {
      return new XmlReader();
    }
    throw new IllegalArgumentException("Неподдерживаемый формат файла: " + fileName);
  }

  public static DataWriter getWriter(String fileName) {
    if (fileName.endsWith(".json")) {
      return new JsonWriter();
    } else if (fileName.endsWith(".xml")) {
      return new XmlWriter();
    }
    throw new IllegalArgumentException("Неподдерживаемый формат файла: " + fileName);
  }
}
