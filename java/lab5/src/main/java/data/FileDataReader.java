package data;

import city.City;
import io.FieldMetaReader;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FileDataReader<T> implements DataReader<T>, Closeable {
  private final BufferedReader reader;

  public FileDataReader(BufferedReader reader) {
    this.reader = reader;
  }
  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }
  @Override
  public Optional<String> readStringWithMeta(BiConsumer<T, String> setter) throws IOException {
    String prompt = FieldMetaReader.buildPrompt(setter);
    String errorMessage = FieldMetaReader.buildErrorMessage(setter);
    boolean nullable = FieldMetaReader.isNullable(setter);

    return readString(prompt, errorMessage, nullable);
  }

  @Override
  public Optional<Long> readLongWithMeta(BiConsumer<T, Long> setter) throws IOException {
    String prompt = FieldMetaReader.buildPrompt(setter);
    String errorMessage = FieldMetaReader.buildErrorMessage(setter);
    boolean nullable = FieldMetaReader.isNullable(setter);
    boolean positive = FieldMetaReader.mustBePositive(setter);
    double min = FieldMetaReader.getMinValue(setter);
    double max = FieldMetaReader.getMaxValue(setter);

    return readLong(prompt, errorMessage, positive, nullable,
            min != Double.NEGATIVE_INFINITY ? (long)min : null,
            max != Double.POSITIVE_INFINITY ? (long)max : null);
  }

  @Override
  public Optional<Integer> readIntWithMeta(BiConsumer<T, Integer> setter) throws IOException {
    String prompt = FieldMetaReader.buildPrompt(setter);
    String errorMessage = FieldMetaReader.buildErrorMessage(setter);
    boolean nullable = FieldMetaReader.isNullable(setter);
    boolean positive = FieldMetaReader.mustBePositive(setter);
    double min = FieldMetaReader.getMinValue(setter);
    double max = FieldMetaReader.getMaxValue(setter);

    return readInt(prompt, errorMessage, positive, nullable,
            min != Double.NEGATIVE_INFINITY ? (int)min : null,
            max != Double.POSITIVE_INFINITY ? (int)max : null);
  }

  @Override
  public Optional<Float> readFloatWithMeta(BiConsumer<T, Float> setter) throws IOException {
    String prompt = FieldMetaReader.buildPrompt(setter);
    String errorMessage = FieldMetaReader.buildErrorMessage(setter);
    boolean nullable = FieldMetaReader.isNullable(setter);
    boolean positive = FieldMetaReader.mustBePositive(setter);
    double min = FieldMetaReader.getMinValue(setter);
    double max = FieldMetaReader.getMaxValue(setter);

    return readFloat(prompt, errorMessage, positive, nullable,
            (float)min, (float)max);
  }

  @Override
  public Optional<Double> readDoubleWithMeta(BiConsumer<T, Double> setter) throws IOException {
    String prompt = FieldMetaReader.buildPrompt(setter);
    String errorMessage = FieldMetaReader.buildErrorMessage(setter);
    boolean nullable = FieldMetaReader.isNullable(setter);
    boolean positive = FieldMetaReader.mustBePositive(setter);
    double min = FieldMetaReader.getMinValue(setter);
    double max = FieldMetaReader.getMaxValue(setter);

    return readDouble(prompt, errorMessage, positive, nullable, min, max);
  }

  @Override
  public <E extends Enum<E>> Optional<E> readEnumWithMeta(
      BiConsumer<T, E> setter, Class<E> enumClass) throws IOException {
    String prompt = FieldMetaReader.buildPrompt(setter);
    String errorMessage = FieldMetaReader.buildErrorMessage(setter);

    return readEnum(prompt, enumClass, errorMessage, FieldMetaReader.isNullable(setter));
  }

  @Override
  public Optional<String> readString(String prompt, String errorMessage, boolean allowNull)
      throws IOException {
    String line = reader.readLine();
    if (line == null || line.trim().isEmpty()) {
      if (allowNull) return Optional.empty();
      throw new IllegalArgumentException(errorMessage);
    }
    return Optional.of(line.trim());
  }

  @Override
  public Optional<Long> readLong(
          String prompt, String errorMessage, boolean mustBePositive, boolean allowNull,
          Long min, Long max) throws IOException {
    return readNumber(errorMessage, mustBePositive, allowNull, min, max, Long::parseLong);
  }

  @Override
  public Optional<Integer> readInt(
          String prompt, String errorMessage, boolean mustBePositive, boolean allowNull,
          Integer min, Integer max) throws IOException {
    return readNumber(errorMessage, mustBePositive, allowNull, min, max, Integer::parseInt);
  }

  @Override
  public Optional<Float> readFloat(
          String prompt, String errorMessage, boolean mustBePositive, boolean allowNull,
          Float min, Float max) throws IOException {
    return readNumber(errorMessage, mustBePositive, allowNull, min, max, Float::parseFloat);
  }

  @Override
  public Optional<Double> readDouble(
          String prompt, String errorMessage, boolean mustBePositive, boolean allowNull,
          Double min, Double max) throws IOException {
    return readNumber(errorMessage, mustBePositive, allowNull, min, max, Double::parseDouble);
  }

  private <V extends Number> Optional<V> readNumber(
          String errorMessage,
          boolean mustBePositive,
          boolean allowNull,
          V min,
          V max,
          Function<String, V> parser) throws IOException {

    String line = reader.readLine();

    // Проверка на null или пустую строку
    if (line == null || line.trim().isEmpty()) {
      if (allowNull) return Optional.empty();
      throw new IllegalArgumentException(errorMessage);
    }

    try {
      V value = parser.apply(line.trim());
      if (mustBePositive && value.doubleValue() <= 0) {
        throw new IllegalArgumentException(errorMessage);
      }
      if (min != null && value.doubleValue() < min.doubleValue()) {
        throw new IllegalArgumentException(errorMessage +
                (mustBePositive ? " " : "") +
                "(мин. значение: " + min + ")");
      }
      if (max != null && value.doubleValue() > max.doubleValue()) {
        throw new IllegalArgumentException(errorMessage +
                (mustBePositive ? " " : "") +
                "(макс. значение: " + max + ")");
      }

      return Optional.of(value);

    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  @Override
  public <E extends Enum<E>> Optional<E> readEnum(
      String prompt, Class<E> enumClass, String errorMessage, boolean allowNull)
      throws IOException {
    E[] values = enumClass.getEnumConstants();

    while (true) {

      String line = reader.readLine();
      if (line == null) {
        throw new IOException("Ошибка чтения ввода.");
      }

      line = line.trim();

      if (allowNull && line.isEmpty()) {
        return Optional.empty();
      }

      if (line.matches("\\d+")) {
        int index = Integer.parseInt(line) - 1;
        if (index >= 0 && index < values.length) {
          return Optional.of(values[index]);
        }
      } else {

        try {
          return Optional.of(Enum.valueOf(enumClass, line.toUpperCase()));
        } catch (IllegalArgumentException ignored) {

        }
      }

      System.out.println("Ошибка: " + errorMessage);
    }
  }
}
