package data;

import city.City;
import java.util.List;
import storage.CityManager;

public class DataProcessor {
  private final CityManager cityManager;

  public DataProcessor(CityManager cityManager) {
    this.cityManager = cityManager;
  }

  public void processData(List<CityData> cityDataList) {
    for (CityData cityData : cityDataList) {
      City city =
          new City(
              cityData.getId(),
              cityData.getName(),
              cityData.getCoordinates(),
              cityData.getCreationDate(),
              cityData.getArea(),
              cityData.getPopulation(),
              cityData.getMetersAboveSeaLevel(),
              cityData.getClimate(),
              cityData.getGovernment(),
              cityData.getStandardOfLiving(),
              cityData.getGovernor());
      cityManager.addCity(city);
    }
  }
}
