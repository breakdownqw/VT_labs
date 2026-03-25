package exceptions;

/** Исключение, возникающее при ошибках валидации данных. */
public final class ValidationException extends RuntimeException {
  /**
   * Конструктор с сообщением об ошибке.
   *
   * @param message сообщение об ошибке.
   */
  public ValidationException(String message) {
    super(message);
  }

  /**
   * Конструктор с сообщением и причиной ошибки.
   *
   * @param message сообщение об ошибке.
   * @param cause причина ошибки.
   */
  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
