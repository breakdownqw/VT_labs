package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.CityData;
import data.DataLoader;
import exceptions.FileReadException;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class JsonLoader implements DataLoader {
  private final ObjectMapper objectMapper;

  public JsonLoader() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  public List<CityData> loadData(String fileName) throws FileReadException {
    try {
      CityData[] cityDataArray = objectMapper.readValue(new File(fileName), CityData[].class);
      return Arrays.asList(cityDataArray);
    } catch (Exception e) {
      throw new FileReadException("Ошибка при чтении JSON-файла: " + fileName, e);
    }
  }
}
