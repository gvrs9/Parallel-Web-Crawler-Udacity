package com.udacity.webcrawler.json;

public class ConfigurationParseException extends RuntimeException {
  public ConfigurationParseException(String message) {
    super(message);
  }

  public ConfigurationParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationParseException(Throwable cause) {
    super(cause);
  }
}
