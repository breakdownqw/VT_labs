package data;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface DataReader<T> {
  Optional<String> readString(String prompt, String errorMessage, boolean allowNull)
          throws IOException;

  Optional<String> readStringWithMeta(BiConsumer<T, String> setter) throws IOException;

  Optional<Long> readLongWithMeta(BiConsumer<T, Long> setter) throws IOException;

  Optional<Integer> readIntWithMeta(BiConsumer<T, Integer> setter) throws IOException;

  Optional<Float> readFloatWithMeta(BiConsumer<T, Float> setter) throws IOException;

  Optional<Double> readDoubleWithMeta(BiConsumer<T, Double> setter) throws IOException;

  <E extends Enum<E>> Optional<E> readEnumWithMeta(BiConsumer<T, E> setter, Class<E> enumClass)
          throws IOException;


  Optional<Long> readLong(
          String prompt,
          String errorMessage,
          boolean mustBePositive,
          boolean allowNull,
          Long min,
          Long max
  ) throws IOException;

  Optional<Integer> readInt(
          String prompt,
          String errorMessage,
          boolean mustBePositive,
          boolean allowNull,
          Integer min,
          Integer max
  ) throws IOException;

  Optional<Float> readFloat(
          String prompt,
          String errorMessage,
          boolean mustBePositive,
          boolean allowNull,
          Float min,
          Float max
  ) throws IOException;

  Optional<Double> readDouble(
          String prompt,
          String errorMessage,
          boolean mustBePositive,
          boolean allowNull,
          Double min,
          Double max
  ) throws IOException;

  <E extends Enum<E>> Optional<E> readEnum(
          String prompt, Class<E> enumClass, String errorMessage, boolean allowNull) throws IOException;
}
