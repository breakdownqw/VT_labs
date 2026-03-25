package exceptions;

public final class FileReadException extends Exception {
  /**
   * Конструктор с сообщением об ошибке.
   *
   * @param message сообщение об ошибке.
   */
  public FileReadException(String message) {
    super(message);
  }

  /**
   * Конструктор с сообщением и причиной ошибки.
   *
   * @param message сообщение об ошибке.
   * @param cause причина ошибки.
   */
  public FileReadException(String message, Throwable cause) {
    super(message, cause);
  }
}
