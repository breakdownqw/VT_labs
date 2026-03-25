package exceptions;

public final class FileWriteException extends Exception {
  /**
   * Конструктор с сообщением об ошибке.
   *
   * @param message сообщение об ошибке.
   */
  public FileWriteException(String message) {
    super(message);
  }

  /**
   * Конструктор с сообщением и причиной ошибки.
   *
   * @param message сообщение об ошибке.
   * @param cause причина ошибки.
   */
  public FileWriteException(String message, Throwable cause) {
    super(message, cause);
  }
}
