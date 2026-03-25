package city;

import exceptions.ValidationException;
import validation.validationService;

/**
 * Класс, представляющий человека (губернатора).
 *
 * <p>Объект этого класса хранит информацию о росте человека. Рост должен быть больше 0. Для
 * валидации значения роста используется сервис {@link validationService}.
 *
 * @see validationService
 */
public class  Human {

  private Float height;

  /**
   * Создает новый объект человека с заданным ростом.
   *
   * <p>Значение роста валидируется с помощью {@link validationService#validateHeight(Float)}. Если
   * значение не проходит валидацию, выбрасывается исключение.
   *
   * @param height рост человека. Должен быть больше 0.
   * @throws ValidationException если значение роста не проходит валидацию.
   */
  public Human(Float height) {
    this.height = height;
  }

  /**
   * Возвращает значение роста человека.
   *
   * @return рост человека. Всегда больше 0.
   */
  public float getHeight() {
    return height;
  }
  public void setHeight(Float height){
    this.height=height;
  }

  /**
   * Возвращает строковое представление объекта человека.
   *
   * @return строковое представление человека.
   */
  @Override
  public String toString() {
    return "city.Human{" + "height=" + height + '}';
  }
}
