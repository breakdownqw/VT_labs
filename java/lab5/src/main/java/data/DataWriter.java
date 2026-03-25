package data;

import exceptions.FileWriteException;
import java.util.List;

public interface DataWriter {
  void writeDataToFile(String fileName, List<CityData> cities) throws FileWriteException;
}
