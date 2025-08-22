package com.udacity.webcrawler.json;

import com.udacity.webcrawler.exceptions.jsonexceptions.ConfigurationParseException;
import com.udacity.webcrawler.utils.JsonUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;
//  private static final ObjectMapper objMapper = new ObjectMapper().disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() throws IOException {
    // TODO: Fill in this method. --> Done!
    try (Reader reader = Files.newBufferedReader(path)) {
      return read(reader);
    }
    // return new CrawlerConfiguration.Builder().build();
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // This is here to get rid of the unused variable warning.
    // Objects.requireNonNull(reader);
    // TODO: Fill in this method --> Done!
    try {
//      return objMapper.readValue(Objects.requireNonNull(reader), CrawlerConfiguration.Builder.class).build();
      return JsonUtils.convertFromJson(reader, CrawlerConfiguration.Builder.class).build();
    } catch (IOException e) {
      throw new ConfigurationParseException("Failed to parse JSON crawler configuration ", e);
//      throw new RuntimeException();
    }
  }
}
