package com.udacity.webcrawler.exceptions.jsonexceptions;

public class CrawlResultWriteException extends RuntimeException {
  public  CrawlResultWriteException(String message) {
    super(message);
  }

  public CrawlResultWriteException(String message, Throwable cause) {
    super(message, cause);
  }

  public CrawlResultWriteException(Throwable cause) {
    super(cause);
  }
}
