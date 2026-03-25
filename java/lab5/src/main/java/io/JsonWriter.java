package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.CityData;
import data.DataWriter;
import exceptions.FileWriteException;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class JsonWriter implements DataWriter {
  private final ObjectMapper objectMapper;

  public JsonWriter() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  public void writeDataToFile(String fileName, List<CityData> cityDataList)
      throws FileWriteException {
    cityDataList.sort(Comparator.comparingInt(CityData::getId));
    try {
      objectMapper.writeValue(new File(fileName), cityDataList);
    } catch (Exception e) {
      throw new FileWriteException("Ошибка при записи JSON-файла: " + fileName, e);
    }
  }
}
