package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class ParallelWebCrawlerRecursiveTask extends RecursiveTask<Boolean> {

  private final String url;
  private final Instant deadline;
  private final int presentDepth;
  private final PageParserFactory parserFactory;
  private final Clock clock;
  private final ConcurrentMap<String, Integer> wordCounts;
  private final ConcurrentSkipListSet<String> visitedUrls;
  private final List<Pattern> ignoredUrls;


  public ParallelWebCrawlerRecursiveTask(String url, Instant deadline, ConcurrentMap<String, Integer> wordCounts, ConcurrentSkipListSet<String> visitedUrls, int maxDepth, PageParserFactory parserFactory, Clock clock, List<Pattern> ignoredUrls) {

    this.url = url;
    this.deadline = deadline;
    this.presentDepth = maxDepth;
    this.parserFactory = parserFactory;
    this.clock = clock;
    this.wordCounts = wordCounts;
    this.visitedUrls = visitedUrls;
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  protected Boolean compute() {
    return null;
  }
}
