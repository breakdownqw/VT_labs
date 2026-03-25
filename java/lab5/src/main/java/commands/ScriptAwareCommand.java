package commands;

import java.io.BufferedReader;
import java.io.IOException;

public interface ScriptAwareCommand {
  void setScriptMode(boolean isScriptMode, BufferedReader scriptReader);
  void close()throws IOException;
}

