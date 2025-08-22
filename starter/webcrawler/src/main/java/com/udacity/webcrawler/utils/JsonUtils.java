package com.udacity.webcrawler.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class JsonUtils {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
          .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
          .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

  private JsonUtils() {
  }

  public static ObjectMapper getRawMapper() {
    return OBJECT_MAPPER;
  }

  public static <T> T convertFromJson(Reader reader, Class<T> type) throws IOException {
    return OBJECT_MAPPER.readValue(reader, type);
  }

  public static void convertToJson(Writer write, Object value, boolean requirePrettyPrint) throws IOException {
    if (requirePrettyPrint) OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(write, value);
    else OBJECT_MAPPER.writeValue(write, value);
  }
}
