package data;

import exceptions.FileReadException;
import java.util.List;

public interface DataLoader {
  List<CityData> loadData(String fileName) throws FileReadException;
}
