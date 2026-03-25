package io;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class FieldMetaReader {
  private static final Map<BiConsumer<?, ?>, MethodHandle> methodHandleCache =
      new ConcurrentHashMap<>();

  public static <T, V> MethodHandle extractMethodHandle(BiConsumer<T, V> setter) {
    return methodHandleCache.computeIfAbsent(
        setter,
        s -> {
          try {
            Method writeReplace = s.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(s);

            String methodName = serializedLambda.getImplMethodName();
            String className = serializedLambda.getImplClass().replace('/', '.');

            // Загружаем класс и получаем метод
            Class<?> targetClass = Class.forName(className);
            Method method =
                Arrays.stream(targetClass.getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException(methodName));

            return MethodHandles.lookup().unreflect(method);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }


  public static <T, V> FieldInput getFieldMeta(BiConsumer<T, V> setter) {
    MethodHandle methodHandle = extractMethodHandle(setter);
    Method method =
        MethodHandles.lookup()
            .revealDirect(methodHandle)
            .reflectAs(Method.class, MethodHandles.lookup());
    return method.getAnnotation(FieldInput.class);
  }

  public static <T, V> String getFieldName(BiConsumer<T, V> setter) {
    MethodHandle methodHandle = extractMethodHandle(setter);
    Method method =
        MethodHandles.lookup()
            .revealDirect(methodHandle)
            .reflectAs(Method.class, MethodHandles.lookup());
    String methodName = method.getName();
    return methodName.startsWith("set") && methodName.length() > 3
        ? Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4)
        : methodName;
  }

  public static <T, V> String buildPrompt(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    return (meta != null && !meta.prompt().isEmpty()) ? meta.prompt() : getFieldName(setter) + ": ";
  }

  public static <T, V> String buildErrorMessage(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    String fieldName = getFieldName(setter);

    // Собираем основное сообщение об ошибке
    String baseMessage = getBaseErrorMessage(meta, fieldName);

    // Добавляем информацию о диапазоне, если он задан
    String rangeMessage = getRangeMessage(meta, fieldName);

    if (!baseMessage.isEmpty() && !rangeMessage.isEmpty()) {
      return baseMessage + ". " + rangeMessage;
    } else if (!baseMessage.isEmpty()) {
      return baseMessage;
    } else if (!rangeMessage.isEmpty()) {
      return rangeMessage;
    }

    return "Некорректное значение для " + fieldName;
  }

  private static String getBaseErrorMessage(FieldInput meta, String fieldName) {
    int flags = meta.flags();

    if ((flags & 0b111) == FieldFlags.NO_RESTRICTIONS) {
      return "";
    }

    return switch (flags & 0b111) {
      case FieldFlags.MUST_BE_POSITIVE -> fieldName + " должно быть больше 0";
      case FieldFlags.MUST_BE_NON_EMPTY -> fieldName + " не может быть пустым";
      case FieldFlags.MUST_BE_POSITIVE_AND_NON_EMPTY ->
              fieldName + " не может быть пустым и должно быть больше 0";
      case FieldFlags.MUST_BE_NEGATIVE -> fieldName + " должно быть отрицательным";
      case FieldFlags.MUST_BE_NEGATIVE_AND_NON_EMPTY ->
              fieldName + " не может быть пустым и должно быть отрицательным";
      default -> "";
    };
  }

  private static String getRangeMessage(FieldInput meta, String fieldName) {
    double min = meta.min();
    double max = meta.max();

    if (min != Double.NEGATIVE_INFINITY && max != Double.POSITIVE_INFINITY) {
      return String.format("%s должно быть между %.2f и %.2f", fieldName, min, max);
    } else if (min != Double.NEGATIVE_INFINITY) {
      return String.format("%s должно быть не меньше %.2f", fieldName, min);
    } else if (max != Double.POSITIVE_INFINITY) {
      return String.format("%s должно быть не больше %.2f", fieldName, max);
    }
    return "";
  }

  public static <T, V> boolean isNullable(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    if (meta == null) return true;
    int flags = meta.flags();
    return (flags & FieldFlags.MUST_BE_NON_EMPTY) == 0;
  }

  public static <T, V> boolean mustBePositive(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    if (meta == null) return false;
    int flags = meta.flags();
    return (flags & FieldFlags.MUST_BE_POSITIVE) != 0;
  }

  public static <T, V> boolean mustBeNegative(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    if (meta == null) return false;
    int flags = meta.flags();
    return (flags & FieldFlags.MUST_BE_NEGATIVE) != 0;
  }

  public static <T, V> boolean mustBeNonEmpty(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    if (meta == null) return false;
    return (meta.flags() & FieldFlags.MUST_BE_NON_EMPTY) != 0;
  }
  public static <T, V> double getMinValue(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    return meta != null ? meta.min() : Double.NEGATIVE_INFINITY;
  }

  public static <T, V> double getMaxValue(BiConsumer<T, V> setter) {
    FieldInput meta = getFieldMeta(setter);
    return meta != null ? meta.max() : Double.POSITIVE_INFINITY;
  }


  public static <T, V> boolean isInRange(BiConsumer<T, V> setter, Number value) {
    if (value == null) return true;

    FieldInput meta = getFieldMeta(setter);
    if (meta == null) return true;

    double num = value.doubleValue();
    return num >= meta.min() && num <= meta.max();
  }
}
