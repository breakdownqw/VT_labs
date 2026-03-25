package io;

import java.io.IOException;
import java.util.Scanner;

public class InputHandler {
  private final Scanner scanner;

  public InputHandler(Scanner scanner) {
    this.scanner = scanner;
  }

  public <T> T inputObject(InputStrategy<T> strategy) throws IOException {
    return strategy.inputObject();
  }
}
