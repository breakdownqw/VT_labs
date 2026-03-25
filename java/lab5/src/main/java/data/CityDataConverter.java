package data;

import city.City;
import java.util.List;
import java.util.stream.Collectors;

public class CityDataConverter {
  public static CityData toCityData(City city) {
    CityData cityData = new CityData();
    cityData.setId(city.getId());
    cityData.setName(city.getName());
    cityData.setCoordinates(city.getCoordinates());
    cityData.setCreationDate(city.getCreationDate());
    cityData.setArea(city.getArea());
    cityData.setPopulation(city.getPopulation());
    cityData.setMetersAboveSeaLevel(city.getMetersAboveSeaLevel());
    cityData.setClimate(city.getClimate());
    cityData.setGovernment(city.getGovernment());
    cityData.setStandardOfLiving(city.getStandardOfLiving());
    cityData.setGovernor(city.getGovernor());
    return cityData;
  }

  public static List<CityData> toCityDataList(List<City> cities) {
    return cities.stream().map(CityDataConverter::toCityData).collect(Collectors.toList());
  }
}
