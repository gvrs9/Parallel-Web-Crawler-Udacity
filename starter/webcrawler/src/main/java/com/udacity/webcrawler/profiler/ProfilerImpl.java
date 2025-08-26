package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "");
    this.startTime = ZonedDateTime.now(clock);
  }

  private void validatingProfiledMethods(Class<?> klass) {
    if (!containsProfiledMethod(klass)) throw new IllegalArgumentException(klass.getName() + " it must have profiled methods!!");
  }

  private boolean containsProfiledMethod(Class<?> klass) {
    for (var method : klass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Profiled.class)) return true;
    }
    return false;
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // TODO: Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.

//    return delegate;

    Objects.requireNonNull(klass, "Class must not be null");
    Objects.requireNonNull(delegate, "Delegate must not be null");

    validatingProfiledMethods(klass);

    Object newProxyInstance = Proxy.newProxyInstance(
            ProfilerImpl.class.getClassLoader(),
            new Class[]{klass},
            (proxyObject, method, args) -> new ProfilingMethodInterceptor(clock, delegate, state, startTime).invoke(proxyObject, method, args)
    );

    return klass.cast(newProxyInstance);
  }

  @Override
  public void writeData(Path path) {
    // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
    Objects.requireNonNull(path, "Path must not be null");

    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, CREATE, APPEND)) {
      writeData(writer);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
