package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилити класс для работы с датой и временем.
 *
 * <p>Этот класс предоставляет методы для получения текущего времени, времени старта программы и
 * форматирования даты и времени в строку.
 *
 * @see LocalDateTime
 * @see DateTimeFormatter
 */
public final class DateUtils {
  private static final LocalDateTime START_TIME = LocalDateTime.now();
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Возвращает время старта программы.
   *
   * @return время старта программы.
   */
  public static LocalDateTime getStartTime() {
    return START_TIME;
  }

  /**
   * Возвращает текущее время.
   *
   * @return текущее время.
   */
  public static LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now();
  }

  /**
   * Форматирует объект {@link LocalDateTime} в строку.
   *
   * @param dateTime объект {@link LocalDateTime} для форматирования.
   * @return строка, представляющая дату и время в формате "yyyy-MM-dd HH:mm:ss".
   */
  public static String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(FORMATTER);
  }
}
