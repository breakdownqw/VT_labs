package validation;

import city.*;
import enums.*;
import exceptions.ValidationException;

public class validationService {

  /** Валидация названия города. */
  public static String validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new ValidationException("Название города не может быть пустым.");
    }
    return name;
  }

  /** Валидация координат. */
  public static Coordinates validateCoordinates(Coordinates coordinates) {
    if (coordinates == null) {
      throw new ValidationException("Координаты не могут быть null.");
    }
    return coordinates;
  }

  /** Валидация площади. */
  public static Long validateArea(Long area) {
    if (area == null || area <= 0) {
      throw new ValidationException("Площадь должна быть больше 0.");
    }
    return area;
  }

  /** Валидация населения. */
  public static Integer validatePopulation(Integer population) {
    if (population == null || population <= 0) {
      throw new ValidationException("Население должно быть больше 0.");
    }
    return population;
  }

  /** Валидация климата. */
  public static Climate validateClimate(Climate climate) {
    if (climate == null) {
      throw new ValidationException("Климат не может быть null.");
    }
    return climate;
  }

  /** Валидация типа правительства. */
  public static Government validateGovernment(Government government) {
    if (government == null) {
      throw new ValidationException("Тип правительства не может быть null.");
    }
    return government;
  }

  /** Валидация уровня жизни. */
  public static StandardOfLiving validateStandardOfLiving(StandardOfLiving standardOfLiving) {
    if (standardOfLiving == null) {
      throw new ValidationException("Уровень жизни не может быть null.");
    }
    return standardOfLiving;
  }

  /** Валидация координаты X. */
  public static double validateX(double x) {
    if (x > 36) {
      throw new ValidationException("Координата X не может быть больше 36.");
    }
    return x;
  }

  /** Валидация роста. */
  public static Float validateHeight(Float height) {
    if (height == null || height <= 0) {
      throw new ValidationException("Рост должен быть больше 0.");
    }
    return height;
  }
}
