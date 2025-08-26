package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.swing.text.html.parser.Parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParallelWebCrawlerRecursiveTask extends RecursiveTask<Boolean> {

  private final String url;
  private final Instant deadline;
  private final int presentDepth;
  private final PageParserFactory parserFactory;
  private final Clock clock;
  private final ConcurrentMap<String, Integer> wordCounts;
  private final ConcurrentSkipListSet<String> visitedUrls;
  private final List<Pattern> ignoredUrls;

  private static final ConcurrentMap<String, Set<String>> robotsCache = new ConcurrentHashMap<>();


  private ParallelWebCrawlerRecursiveTask(final Builder builder) {

    this.url = builder.url;
    this.deadline = builder.deadline;
    this.presentDepth = builder.presentDepth;
    this.parserFactory = builder.parserFactory;
    this.clock = builder.clock;
    this.wordCounts = builder.wordCounts;
    this.visitedUrls = builder.visitedUrls;
    this.ignoredUrls = builder.ignoredUrls;
  }

  public static class Builder {
    private String url;
    private Instant deadline;
    private int presentDepth;
    private PageParserFactory parserFactory;
    private Clock clock;
    private ConcurrentMap<String, Integer> wordCounts;
    private ConcurrentSkipListSet<String> visitedUrls;
    private List<Pattern> ignoredUrls;

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setDeadline(final Instant deadline) {
      this.deadline = deadline;
      return this;
    }

    public Builder setPresentDepth(final int presentDepth) {
      this.presentDepth = presentDepth;
      return this;
    }

    public Builder setWordCounts(final ConcurrentMap<String, Integer> wordCounts) {
      this.wordCounts = wordCounts;
      return this;
    }

    public Builder setVisitedUrls(final ConcurrentSkipListSet<String> visitedUrls) {
      this.visitedUrls = visitedUrls;
      return this;
    }

    public Builder setParserFactory(final PageParserFactory parserFactory) {
      this.parserFactory = parserFactory;
      return this;
    }

    public Builder setClock(final Clock clock) {
      this.clock = clock;
      return this;
    }

    public Builder setIgnoredUrls(final List<Pattern> ignoredUrls) {
      this.ignoredUrls = ignoredUrls;
      return this;
    }

    public ParallelWebCrawlerRecursiveTask build() {
      if (url == null || deadline == null || wordCounts == null ||
              visitedUrls == null || parserFactory == null || clock == null || ignoredUrls == null) {
        throw new IllegalStateException("Missing required fields for ParallelWebCrawlerRecursiveTask");
      }
      return new ParallelWebCrawlerRecursiveTask(this);
    }
  }

  @Override
  protected Boolean compute() {
    if (presentDepth == 0 || clock.instant().isAfter(deadline)) return false;
    if (isIgnored(url) || isDisallowedByRobotsTxt(url)) return false;
    if (!visitedUrls.add(url)) return false;

    try {

      PageParser.Result result = parserFactory.get(url).parse();
      mergeWordCounts(result.getWordCounts());

      List<ParallelWebCrawlerRecursiveTask> subtasks = result.getLinks().stream()
              .map(link -> new Builder()
                      .setUrl(link)
                      .setDeadline(deadline)
                      .setPresentDepth(presentDepth - 1)
                      .setWordCounts(wordCounts)
                      .setVisitedUrls(visitedUrls)
                      .setParserFactory(parserFactory)
                      .setClock(clock)
                      .setIgnoredUrls(ignoredUrls)
                      .build())
              .collect(Collectors.toList());

      invokeAll(subtasks);
      subtasks.forEach(ForkJoinTask::join);

      return true;

    } catch (Exception e) {
      System.err.println("Failed to crawl " + url + ": " + e.getMessage());
      return false;
    }
  }

  private void mergeWordCounts(final Map<String, Integer> counts) {
    counts.forEach((word, count) -> wordCounts.merge(word, count, Integer::sum));
  }

  private boolean isIgnored(final String url) {
    return ignoredUrls.stream().anyMatch(pattern -> pattern.matcher(url).matches());
  }

  private boolean isDisallowedByRobotsTxt(final String urlString) {
    try {
      URL u = new URL(urlString);
      String domainKey = u.getProtocol() + "://" + u.getHost();

      Set<String> disallowed = robotsCache.computeIfAbsent(domainKey, this::fetchRobotsTxt);

      String path = u.getPath();
      return disallowed.stream().anyMatch(path::startsWith);
    } catch (IOException e) {
      return false;
    }
  }

  private Set<String> fetchRobotsTxt(final String domain) {
    try {
      URL robotsTxtUrl = new URL(domain + "/robots.txt");
      InputStream inputStream = robotsTxtUrl.openStream();

      try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        return reader.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("Disallow:"))
                .map(line -> line.substring("Disallow:".length()).trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
      }

    } catch (IOException e) {
      return Set.of();
    }
  }
}
