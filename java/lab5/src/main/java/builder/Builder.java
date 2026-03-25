package builder;

import java.util.function.BiConsumer;

public class Builder<T> {
  private final T instance;

  public Builder(T instance) {
    this.instance = instance;
  }

  public Builder<T> set(BiConsumer<T, Number> setter, Number value) {
    setter.accept(instance, value);
    return this;
  }

  public <E extends Enum<E>> Builder<T> set(BiConsumer<T, E> setter, E value) {
    setter.accept(instance, value);
    return this;
  }

  public <V> Builder<T> set(BiConsumer<T, V> setter, V value) {
    setter.accept(instance, value);
    return this;
  }

  public T build() {
    return instance;
  }
}
