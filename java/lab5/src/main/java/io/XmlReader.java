package io;

import static util.XmlUtils.*;

import city.Coordinates;
import city.Human;
import data.CityData;
import data.DataLoader;
import enums.Climate;
import enums.Government;
import enums.StandardOfLiving;
import exceptions.FileReadException;
import exceptions.ValidationException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import validation.validationService;

/**
 * Класс для чтения данных из XML-файла и их преобразования в объекты {@link CityData}.
 *
 * <p>Этот класс предоставляет функциональность для чтения данных о городах из XML-файла и их
 * преобразования в объекты класса {@link CityData}. Для валидации данных используется {@link
 * validationService}.
 *
 * <p>В случае ошибок при чтении файла или валидации данных выбрасываются исключения {@link
 * FileReadException} и {@link ValidationException}.
 *
 * @see CityData
 * @see validationService
 * @see FileReadException
 * @see ValidationException
 */
public class XmlReader implements DataLoader {

  /** Создает объект для чтения данных из XML-файла. */
  public XmlReader() {}

  /**
   * Читает данные о городах из XML-файла и преобразует их в список объектов {@link CityData}.
   *
   * @param fileName имя файла, из которого будут читаться данные.
   * @return список объектов {@link CityData}.
   * @throws FileReadException если произошла ошибка при чтении файла.
   */
  @Override
  public List<CityData> loadData(String fileName) throws FileReadException {
    List<CityData> cities = new ArrayList<>();
    try (InputStreamReader reader =
        new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8)) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new FileInputStream(fileName));
      document.getDocumentElement().normalize();

      NodeList cityNodes = document.getElementsByTagName("city");
      for (int i = 0; i < cityNodes.getLength(); i++) {
        Node node = cityNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          try {
            Element element = (Element) node;
            CityData city = parseCity(element);
            cities.add(city);
          } catch (ValidationException e) {
            throw new ValidationException("Ошибка валидации города: " + e.getMessage(), e);
          }
        }
      }
    } catch (Exception e) {
      throw new FileReadException("Ошибка при чтении XML-файла: " + fileName, e);
    }
    return cities;
  }

  /**
   * Преобразует XML-элемент в объект {@link CityData}.
   *
   * @param element XML-элемент города.
   * @return объект {@link CityData}.
   * @throws ValidationException если данные не проходят валидацию.
   */
  private CityData parseCity(Element element) throws ValidationException {
    CityData cityData = new CityData();
    cityData.setId(getIntValue(element, "id"));
    cityData.setName(validationService.validateName(getTextValue(element, "name")));
    cityData.setCoordinates(
        parseCoordinates((Element) element.getElementsByTagName("coordinates").item(0)));
    cityData.setCreationDate(parseCreationDate(getTextValue(element, "creationDate")));
    cityData.setArea(validationService.validateArea(getLongValue(element, "area")));
    cityData.setPopulation(
        validationService.validatePopulation(getIntValue(element, "population")));
    cityData.setMetersAboveSeaLevel(getFloatValue(element, "metersAboveSeaLevel"));
    cityData.setClimate(parseEnum(element, "climate", Climate.class));
    cityData.setGovernment(parseEnum(element, "government", Government.class));
    cityData.setStandardOfLiving(parseEnum(element, "standardOfLiving", StandardOfLiving.class));
    cityData.setGovernor(parseHuman((Element) element.getElementsByTagName("governor").item(0)));

    return cityData;
  }

  /**
   * Парсит строку с датой создания в объект {@link LocalDateTime}.
   *
   * @param dateString строка с датой.
   * @return объект {@link LocalDateTime}.
   * @throws ValidationException если строка с датой некорректна.
   */
  private static LocalDateTime parseCreationDate(String dateString) throws ValidationException {
    try {
      return LocalDateTime.parse(dateString);
    } catch (Exception e) {
      throw new ValidationException("Некорректный формат даты: " + dateString);
    }
  }

  /**
   * Парсит XML-элемент координат в объект {@link Coordinates}.
   *
   * @param element XML-элемент координат.
   * @return объект {@link Coordinates}.
   * @throws ValidationException если координаты отсутствуют или не проходят валидацию.
   */
  private Coordinates parseCoordinates(Element element) throws ValidationException {
    if (element == null) throw new ValidationException("Координаты отсутствуют");
    double x = validationService.validateX(getDoubleValue(element, "x"));
    int y = getIntValue(element, "y");
    return new Coordinates(x, y);
  }

  /**
   * Парсит XML-элемент губернатора в объект {@link Human}.
   *
   * @param element XML-элемент губернатора.
   * @return объект {@link Human} или {@code null}, если элемент отсутствует.
   * @throws ValidationException если данные не проходят валидацию.
   */
  private Human parseHuman(Element element) throws ValidationException {
    if (element == null) return null;
    Float height = validationService.validateHeight(getFloatValue(element, "height"));
    return new Human(height);
  }

  /**
   * Парсит значение перечисления из XML-элемента.
   *
   * @param element XML-элемент.
   * @param tagName имя тега, содержащего значение перечисления.
   * @param enumClass класс перечисления.
   * @param <T> тип перечисления.
   * @return значение перечисления.
   * @throws ValidationException если значение некорректно.
   */
  private static <T extends Enum<T>> T parseEnum(
      Element element, String tagName, Class<T> enumClass) throws ValidationException {
    String value = getTextValue(element, tagName);
    try {
      return Enum.valueOf(enumClass, value);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Некорректное значение для " + tagName + ": " + value);
    }
  }
}
