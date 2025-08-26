package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Helper class that records method performance data from the method interceptor.
 */
final class ProfilingState {
  private final Map<String, Duration> methodDurations = new ConcurrentHashMap<>();
  private final Map<String, Map<Long, AtomicLong>> threadCallCounts = new ConcurrentHashMap<>();

  /**
   * Records the given method invocation data.
   *
   * @param callingClass the Java class of the object that called the method.
   * @param method       the method that was called.
   * @param elapsed      the amount of time that passed while the method was called.
   */
  void record(Class<?> callingClass, Method method, Duration elapsed, long threadId) {
    Objects.requireNonNull(callingClass);
    Objects.requireNonNull(method);
    Objects.requireNonNull(elapsed);

    if (elapsed.isNegative()) {
      throw new IllegalArgumentException("negative elapsed time");
    }

    String key = formatMethodCall(callingClass, method);

    methodDurations.compute(key, (k, v) -> (v == null) ? elapsed : v.plus(elapsed));

    threadCallCounts
            .computeIfAbsent(key, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(threadId, t -> new AtomicLong())
            .incrementAndGet();
  }

  /**
   * Writes the method invocation data to the given {@link Writer}.
   *
   * <p>Recorded data is aggregated across calls to the same method. For example, suppose
   * {@link #record(Class, Method, Duration) record} is called three times for the same method
   * {@code M()}, with each invocation taking 1 second. The total {@link Duration} reported by
   * this {@code write()} method for {@code M()} should be 3 seconds.
   */
  void write(Writer writer) throws IOException {
    for (String key : methodDurations.keySet()) {
      Duration total = methodDurations.get(key);

      // Aggregate total calls across all threads
      long totalCalls = threadCallCounts
              .getOrDefault(key, Map.of())
              .values()
              .stream()
              .mapToLong(AtomicLong::get)
              .sum();

      writer.write(key + " took " + formatDuration(total));
      if (totalCalls > 0) {
        writer.write(" (called " + totalCalls + " times)");
      }
      writer.write(System.lineSeparator());

      Map<Long, AtomicLong> threadCounts = threadCallCounts.getOrDefault(key, Map.of());
      for (Map.Entry<Long, AtomicLong> entry : threadCounts.entrySet()) {
        long threadId = entry.getKey();
        long calls = entry.getValue().get();

        Duration avgDuration = total.dividedBy((calls == 0 ? 1 : calls));

        writer.write("[Thread ID: " + threadId + " (called " + calls + " times)]");
        writer.write(" - Average duration: " + formatDuration(avgDuration));
        writer.write(System.lineSeparator());
      }
      writer.write(System.lineSeparator());
    }
  }

  /**
   * Formats the given method call for writing to a text file.
   *
   * @param callingClass the Java class of the object whose method was invoked.
   * @param method       the Java method that was invoked.
   * @return a string representation of the method call.
   */
  private static String formatMethodCall(Class<?> callingClass, Method method) {
    return String.format("%s#%s", callingClass.getName(), method.getName());
  }

  /**
   * Formats the given {@link Duration} for writing to a text file.
   */
  private static String formatDuration(Duration duration) {
    return String.format(
            "%sm %ss %sms", duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());
  }
}
