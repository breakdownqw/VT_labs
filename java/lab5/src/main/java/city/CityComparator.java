package city;

import java.util.Comparator;

/**
 * Компаратор для сравнения объектов класса {@link City}.
 *
 * <p>Этот компаратор использует множество критериев для сравнения двух объектов {@link City}: 1.
 * Сравниваются по имени {@link City#getName()}. 2. Если имена одинаковые, сравниваются координаты
 * {@link City#getCoordinates()} по оси X и затем по оси Y. 3. Если координаты также одинаковые,
 * сравниваются площади {@link City#getArea()}. 4. Если площади равны, то сравнивается численность
 * населения {@link City#getPopulation()}. 5. Если население одинаково, сравнивается высота над
 * уровнем моря {@link City#getMetersAboveSeaLevel()}, где значение {@code null} считается меньшим,
 * чем любое число. 6. Если высоты одинаковы или оба значения равны {@code null}, сравнивается
 * климат {@link City#getClimate()}. 7. Если климат одинаков, то сравнивается форма правительства
 * {@link City#getGovernment()}. 8. Затем сравнивается уровень жизни {@link
 * City#getStandardOfLiving()}. 9. Если все вышеописанные параметры равны, сравнивается высота
 * губернатора {@link City#getGovernor()}, где {@code null} считается меньшим, чем любое значение.
 *
 * <p>Все критерии сравнения выполняются поочередно, и если два города равны по одному из критериев,
 * то используется следующий. Если все поля одинаковы, метод возвращает {@code 0}.
 *
 * @see City
 * @see City#getName()
 * @see City#getCoordinates()
 * @see City#getArea()
 * @see City#getPopulation()
 * @see City#getMetersAboveSeaLevel()
 * @see City#getClimate()
 * @see City#getGovernment()
 * @see City#getStandardOfLiving()
 * @see City#getGovernor()
 */
public final class CityComparator implements Comparator<City> {
  @Override
  public int compare(City city1, City city2) {
    int nameCompare = city1.getName().compareTo(city2.getName());
    if (nameCompare != 0) return nameCompare;


    int xCompare = Double.compare(city1.getCoordinates().getX(), city2.getCoordinates().getX());
    if (xCompare != 0) return xCompare;
    int yCompare = Integer.compare(city1.getCoordinates().getY(), city2.getCoordinates().getY());
    if (yCompare != 0) return yCompare;


    int areaCompare = Long.compare(city1.getArea(), city2.getArea());
    if (areaCompare != 0) return areaCompare;


    int populationCompare = Integer.compare(city1.getPopulation(), city2.getPopulation());
    if (populationCompare != 0) return populationCompare;


    if (city1.getMetersAboveSeaLevel() == null && city2.getMetersAboveSeaLevel() != null) return -1;
    if (city1.getMetersAboveSeaLevel() != null && city2.getMetersAboveSeaLevel() == null) return 1;
    if (city1.getMetersAboveSeaLevel() != null && city2.getMetersAboveSeaLevel() != null) {
      int metersCompare =
          Float.compare(city1.getMetersAboveSeaLevel(), city2.getMetersAboveSeaLevel());
      if (metersCompare != 0) return metersCompare;
    }


    int climateCompare = city1.getClimate().compareTo(city2.getClimate());
    if (climateCompare != 0) return climateCompare;


    int governmentCompare = city1.getGovernment().compareTo(city2.getGovernment());
    if (governmentCompare != 0) return governmentCompare;


    int standardOfLivingCompare =
        city1.getStandardOfLiving().compareTo(city2.getStandardOfLiving());
    if (standardOfLivingCompare != 0) return standardOfLivingCompare;


    Float height1 = (city1.getGovernor() != null) ? city1.getGovernor().getHeight() : null;
    Float height2 = (city2.getGovernor() != null) ? city2.getGovernor().getHeight() : null;
    if (height1 == null && height2 != null) return -1;
    if (height1 != null && height2 == null) return 1;
    if (height1 != null && height2 != null) {
      return Float.compare(height1, height2);
    }


    return 0;
  }
}
