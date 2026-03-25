package storage;

import city.City;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import util.DateUtils;
import util.IdGenerator;

/**
 * Класс для управления коллекцией городов.
 *
 * <p>Этот класс предоставляет методы для работы с коллекцией городов, включая добавление,
 * обновление, удаление, фильтрацию и сортировку. Коллекция хранится в виде {@link Hashtable}, где
 * ключом является ID города, а значением — объект {@link City}.
 *
 * <p>Класс также отслеживает время инициализации коллекции и время последнего изменения.
 *
 * @see City
 * @see Hashtable
 */
public class CityManager {
  private final Hashtable<Integer, City> collection;
  private final LocalDateTime initializationTime;
  private LocalDateTime lastUpdateTime;

  /**
   * Создает новый объект для управления коллекцией городов.
   *
   * <p>Время инициализации устанавливается на момент создания объекта, а время последнего изменения
   * — на текущее время.
   */
  public CityManager() {
    this.collection = new Hashtable<>();
    this.initializationTime = DateUtils.getStartTime();
    this.lastUpdateTime = DateUtils.getCurrentDateTime();
  }

  /**
   * Добавляет город в коллекцию.
   *
   * @param city город для добавления.
   * @throws IllegalArgumentException если город равен {@code null} или уже существует в коллекции.
   */
  public void addCity(City city) {
    if (city == null) {
      throw new IllegalArgumentException("Город не может быть null.");
    }
    if (collection.containsValue(city)) {
      throw new IllegalArgumentException("Город уже существует в коллекции.");
    }
    collection.put(city.getId(), city);
    updateLastModifiedTime();
  }

  /**
   * Обновляет город по ID.
   *
   * @param id ID города для обновления.
   * @param newCity новый объект города.
   * @throws IllegalArgumentException если город с указанным ID не найден.
   */
  public void updateCity(int id, City newCity) {
    if (!collection.containsKey(id)) {
      throw new IllegalArgumentException("Город с ID " + id + " не найден.");
    }
    newCity.setId(id);
    collection.put(id, newCity);
    updateLastModifiedTime();
  }

  public Set<Integer> getAllIds() {
    return new HashSet<>(collection.keySet());
  }

  /**
   * Удаляет город по ID.
   */
  public void removeCity(int id) {
    if (!collection.containsKey(id)) {
      throw new IllegalArgumentException("Город с ID " + id + " не найден.");
    }
    collection.remove(id);
    IdGenerator.releaseId(id);
    updateLastModifiedTime();
  }

  /**
   * Возвращает город по ID.
   *
   * @param id ID города.
   * @return объект города или {@code null}, если город не найден.
   */
  public City getCity(int id) {
    return collection.get(id);
  }

  /**
   * Возвращает все города в коллекции.
   *
   * @return список всех городов.
   */
  public List<City> getAllCities() {
    return new ArrayList<>(collection.values());
  }

  /**
   * Возвращает максимальный ID, присутствующий в коллекции.
   *
   * @return максимальный ID в коллекции или {@code -1}, если коллекция пуста.
   */
  public int getMaxId() {
    if (collection.isEmpty()) {
      return -1; // Возвращаем -1, если коллекция пуста
    }
    return Collections.max(collection.keySet());
  }

  /** Очищает коллекцию. */
  public void clearCollection() {
    updateLastModifiedTime();
    collection.clear();
  }

  /**
   * Удаляет элементы, удовлетворяющие условию.
   *
   * @param condition условие для удаления.
   * @return количество удаленных элементов.
   */
  public int removeIf(Predicate<City> condition) {
    int initialSize = collection.size();
    collection.values().removeIf(condition);
    if (initialSize != collection.size()) {
      updateLastModifiedTime();
    }
    return initialSize - collection.size();
  }

  /**
   * Возвращает размер коллекции.
   *
   * @return количество городов в коллекции.
   */
  public int getCollectionSize() {
    return collection.size();
  }

  /**
   * Фильтрует города по началу имени.
   *
   * @param prefix префикс для фильтрации.
   * @throws IllegalArgumentException если префикс равен {@code null} или пуст.
   */
  public void filterByName(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      throw new IllegalArgumentException("Префикс не может быть пустым.");
    }
    updateLastModifiedTime();
    List<City> filteredCities =
        collection.values().stream().filter(city -> city.getName().startsWith(prefix)).toList();

    filteredCities.forEach(System.out::println); // Вывод в консоль
  }

  /**
   * Возвращает коллекцию городов.
   *
   * @return коллекция городов.
   */
  public Hashtable<Integer, City> getCollection() {
    return collection;
  }

  /**
   * Возвращает время инициализации коллекции.
   *
   * @return время инициализации коллекции.
   */
  public LocalDateTime getInitializationTime() {
    return initializationTime;
  }

  /**
   * Возвращает время последнего изменения коллекции.
   *
   * @return время последнего изменения коллекции.
   */
  public LocalDateTime getLastUpdateTime() {
    return lastUpdateTime;
  }

  /** Обновляет время последнего изменения коллекции. */
  public void updateLastModifiedTime() {
    lastUpdateTime = DateUtils.getCurrentDateTime();
  }
}
