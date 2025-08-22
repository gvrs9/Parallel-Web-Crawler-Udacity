package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    // TODO: Write the crawl results to a JSON file (or System.out if the file name is empty) --> Done.
    // Use this to just execute this task -->  java -classpath target/udacity-webcrawler-1.0.jar com.udacity.webcrawler.main.WebCrawlerMain src/main/java/com/udacity/webcrawler/main/config/sample_config_sequential.json

    String resultPath = config.getResultPath();

    if (resultPath.isBlank()) {
      Writer stdoutWriter = new BufferedWriter(new OutputStreamWriter(System.out));
      resultWriter.write(stdoutWriter);
      stdoutWriter.flush();
    } else {
      Path path = Path.of(resultPath.trim());
      resultWriter.write(path);
    }

    // TODO: Write the profile data to a text file (or System.out if the file name is empty) --> Done.

    String profileOutputPath = config.getProfileOutputPath();
    if (profileOutputPath.isBlank()) {
      Writer stdoutWriter = new BufferedWriter(new OutputStreamWriter(System.out));
      profiler.writeData(stdoutWriter);
      stdoutWriter.flush();
    } else {
      Path path = Path.of(profileOutputPath.trim());
      profiler.writeData(path);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
