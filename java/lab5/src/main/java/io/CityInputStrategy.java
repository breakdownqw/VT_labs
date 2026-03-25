package io;

import builder.Builder;
import city.City;
import city.Coordinates;
import city.Human;
import data.DataReader;
import enums.Climate;
import enums.Government;
import enums.StandardOfLiving;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiConsumer;
import util.DateUtils;
import util.IdGenerator;

public class CityInputStrategy implements InputStrategy<City> {
  private final DataReader<City> reader;


  public CityInputStrategy(DataReader<City> reader) {
    this.reader = reader;
  }

  @Override
  public City inputObject() throws IOException {
    Builder<City> builder = new Builder<>(new City());
    return builder
            .set(City::setId, IdGenerator.getNextId())
            .set(City::setCreationDate, DateUtils.getCurrentDateTime())
            .set(
                    City::setName,
                    reader
                            .readStringWithMeta((BiConsumer<City, String> & Serializable) City::setName)
                            .orElse(null))
            .set(City::setCoordinates, readCoordinates())
            .set(
                    City::setArea,
                    reader
                            .readLongWithMeta((BiConsumer<City, Long> & Serializable) City::setArea)
                            .orElse(null))
            .set(
                    City::setPopulation,
                    reader
                            .readIntWithMeta((BiConsumer<City, Integer> & Serializable) City::setPopulation)
                            .orElse(null))
            .set(
                    City::setMetersAboveSeaLevel,
                    reader
                            .readFloatWithMeta(
                                    (BiConsumer<City, Float> & Serializable) City::setMetersAboveSeaLevel)
                            .orElse(null))
            .set(
                    City::setClimate,
                    reader
                            .readEnumWithMeta(
                                    (BiConsumer<City, Climate> & Serializable) City::setClimate, Climate.class)
                            .orElse(null))
            .set(
                    City::setGovernment,
                    reader
                            .readEnumWithMeta(
                                    (BiConsumer<City, Government> & Serializable) City::setGovernment,
                                    Government.class)
                            .orElse(null))
            .set(
                    City::setStandardOfLiving,
                    reader
                            .readEnumWithMeta(
                                    (BiConsumer<City, StandardOfLiving> & Serializable) City::setStandardOfLiving,
                                    StandardOfLiving.class)
                            .orElse(null))
            .set(City::setGovernor, readGovernor())
            .build();
  }

  private Coordinates readCoordinates() throws IOException {
    double x = readXCoordinate();
    int y = readYCoordinate();
    return new Coordinates(x, y);
  }

  private double readXCoordinate() throws IOException {
    while (true) {
      Optional<Double> xOpt = reader.readDouble(
              "Координата X (максимум 36): ",
              "Значение должно быть не больше 36",
              false,
              false,
              null,
              36.0
      );

      if (xOpt.isPresent()) {
        return xOpt.get();
      }
      System.out.println("Ошибка: Координата X обязательна.");
    }
  }

  private int readYCoordinate() throws IOException {
    while (true) {
      Optional<Integer> yOpt = reader.readInt(
              "Координата Y: ",
              "Введите корректное число",
              false,
              false,
              null,
              null
      );

      if (yOpt.isPresent()) {
        return yOpt.get();
      }
      System.out.println("Ошибка: Координата Y обязательна.");
    }
  }

  private Human readGovernor() throws IOException {
    Optional<Float> height = reader.readFloat(
            "Рост губернатора (можно оставить пустым): ",
            "Введите корректное значение роста",
            false,
            true,
            null,
            null
    );
    return height.map(Human::new).orElse(null);
  }
}
