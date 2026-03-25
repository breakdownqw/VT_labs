package data;

import city.Coordinates;
import city.Human;
import enums.Climate;
import enums.Government;
import enums.StandardOfLiving;
import java.time.LocalDateTime;

public class CityData {
  private Integer id;
  private String name;
  private Coordinates coordinates;
  private LocalDateTime creationDate;
  private Long area;
  private Integer population;
  private Float metersAboveSeaLevel;
  private Climate climate;
  private Government government;
  private StandardOfLiving standardOfLiving;
  private Human governor;

  // Геттеры и сеттеры
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public Long getArea() {
    return area;
  }

  public void setArea(Long area) {
    this.area = area;
  }

  public Integer getPopulation() {
    return population;
  }

  public void setPopulation(Integer population) {
    this.population = population;
  }

  public Float getMetersAboveSeaLevel() {
    return metersAboveSeaLevel;
  }

  public void setMetersAboveSeaLevel(Float metersAboveSeaLevel) {
    this.metersAboveSeaLevel = metersAboveSeaLevel;
  }

  public Climate getClimate() {
    return climate;
  }

  public void setClimate(Climate climate) {
    this.climate = climate;
  }

  public Government getGovernment() {
    return government;
  }

  public void setGovernment(Government government) {
    this.government = government;
  }

  public StandardOfLiving getStandardOfLiving() {
    return standardOfLiving;
  }

  public void setStandardOfLiving(StandardOfLiving standardOfLiving) {
    this.standardOfLiving = standardOfLiving;
  }

  public Human getGovernor() {
    return governor;
  }

  public void setGovernor(Human governor) {
    this.governor = governor;
  }
}
