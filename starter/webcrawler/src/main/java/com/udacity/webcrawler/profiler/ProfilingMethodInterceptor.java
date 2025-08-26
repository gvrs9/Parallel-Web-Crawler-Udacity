package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ZonedDateTime startTime;
  private final ProfilingState state;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state, ZonedDateTime startTime) {
    this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    this.delegate = Objects.requireNonNull(delegate, "Delegate must not be null");
    this.state = Objects.requireNonNull(state, "ProfilingState must not be null");
    this.startTime = Objects.requireNonNull(startTime, "Start time must not be null");
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
//    return null;

    boolean profiled = method.isAnnotationPresent(Profiled.class);
    Instant start = profiled ? clock.instant() : null;

    try {
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      if (profiled && start != null) {
        Duration duration = Duration.between(start, clock.instant());
        long threadId = Thread.currentThread().getId();
        state.record(delegate.getClass(), method, duration, threadId);
      }
    }
  }
}
