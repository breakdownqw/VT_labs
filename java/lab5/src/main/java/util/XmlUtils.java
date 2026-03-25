package util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Утилитарный класс для работы с XML-документами.
 *
 * <p>Этот класс предоставляет методы для извлечения значений из XML-элементов. Методы поддерживают
 * извлечение строковых, целочисленных, длинных целых, вещественных и чисел с плавающей точкой
 * значений из указанных тегов.
 *
 * <p>Класс является утилитарным и не предназначен для создания экземпляров.
 *
 * @see Element
 * @see NodeList
 */
public final class XmlUtils {
  private XmlUtils() {}

  /**
   * Возвращает текстовое значение из указанного тега XML-элемента.
   *
   * @param element XML-элемент, из которого извлекается значение.
   * @param tagName имя тега, значение которого нужно извлечь.
   * @return текстовое значение тега или пустая строка, если тег отсутствует.
   */
  public static String getTextValue(Element element, String tagName) {
    NodeList nodeList = element.getElementsByTagName(tagName);
    if (nodeList.getLength() == 0) return "";
    return nodeList.item(0).getTextContent().trim();
  }

  /**
   * Возвращает целочисленное значение из указанного тега XML-элемента.
   *
   * @param element XML-элемент, из которого извлекается значение.
   * @param tagName имя тега, значение которого нужно извлечь.
   * @return целочисленное значение тега.
   * @throws NumberFormatException если значение тега не может быть преобразовано в целое число.
   */
  public static int getIntValue(Element element, String tagName) {
    return Integer.parseInt(getTextValue(element, tagName));
  }

  /**
   * Возвращает длинное целое значение из указанного тега XML-элемента.
   *
   * @param element XML-элемент, из которого извлекается значение.
   * @param tagName имя тега, значение которого нужно извлечь.
   * @return длинное целое значение тега.
   * @throws NumberFormatException если значение тега не может быть преобразовано в длинное целое
   *     число.
   */
  public static long getLongValue(Element element, String tagName) {
    return Long.parseLong(getTextValue(element, tagName));
  }

  /**
   * Возвращает значение с плавающей точкой из указанного тега XML-элемента.
   *
   * @param element XML-элемент, из которого извлекается значение.
   * @param tagName имя тега, значение которого нужно извлечь.
   * @return значение с плавающей точкой тега или {@code null}, если тег пуст.
   * @throws NumberFormatException если значение тега не может быть преобразовано в число с
   *     плавающей точкой.
   */
  public static Float getFloatValue(Element element, String tagName) {
    String text = getTextValue(element, tagName);
    if (text.isEmpty()) {
      return null;
    }
    return Float.parseFloat(text);
  }

  /**
   * Возвращает вещественное значение из указанного тега XML-элемента.
   *
   * @param element XML-элемент, из которого извлекается значение.
   * @param tagName имя тега, значение которого нужно извлечь.
   * @return вещественное значение тега.
   * @throws NumberFormatException если значение тега не может быть преобразовано в вещественное
   *     число.
   */
  public static double getDoubleValue(Element element, String tagName) {
    return Double.parseDouble(getTextValue(element, tagName));
  }
}
