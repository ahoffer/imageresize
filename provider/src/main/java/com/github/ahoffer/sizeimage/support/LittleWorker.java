package com.github.ahoffer.sizeimage.support;

import static com.github.ahoffer.sizeimage.support.MessageConstants.EXECUTION_EXCEPTION;
import static com.github.ahoffer.sizeimage.support.MessageConstants.THREAD_INTERRUPTED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.TIMEOUT;

import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.ImageSizer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Hide boiler plate code for running tasks */
public class LittleWorker implements AutoCloseable {

  ExecutorService executor;
  ImageSizer caller;
  long timeout;
  TimeUnit unit;

  /**
   * Constructor. Pass it the ImageSizer object who will submit the task to run. This is done so the
   * LittleWorker can add messages to the image sizer as it needs to. To use the LittleWorker
   * without an ImageSizer, create a new instance of NullImageSizer and pass it to the constructor.
   * Pass also the maximum time the LittleWorker will run a task before killing it.
   *
   * @param caller
   * @param timeout
   * @param unit
   */
  public LittleWorker(ImageSizer caller, long timeout, TimeUnit unit) {
    this.caller = caller;
    setTimeout(timeout, unit);
    executor =
        Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable);
              // Do not let this thread prevent the JVM from terminating
              thread.setDaemon(true);
              return thread;
            });
  }

  /**
   * Set the amount of time to elapse before the worker thread is terminated.
   *
   * @param timeout
   * @param unit
   */
  public void setTimeout(long timeout, TimeUnit unit) {
    this.timeout = timeout;
    this.unit = unit;
  }

  /**
   * Primary method on this class. Pass it a Callable. It will run the callable in the another
   * thread, then return the results. If the execution passes the timeout threshold, the thread is
   * killed. If there is a failure, error messages are added to the ImageSizer. This is a
   * synchronous method. Although the task is run in another thread, this method blocks until the
   * task completes, or the time permitted to the task has elapsed. The time spent executing the
   * callable is also added as an informational message.
   *
   * @param supplier
   * @param <T> For typical usage, the type T will be a BufferedImage
   * @return <T>
   */
  public <T> T doThis(Callable<T> supplier) {
    Instant start = Instant.now();
    Future<T> future = executor.submit(supplier);
    T result = null;
    try {
      // Wait for the task to complete or time-out.
      result = future.get(timeout, unit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      caller.addMessage(
          new BeLittlingMessageImpl(
              THREAD_INTERRUPTED,
              BeLittlingSeverity.ERROR,
              "Caught InterruptedException. No automatic retry.",
              e));
    } catch (ExecutionException e) {
      caller.addMessage(
          new BeLittlingMessageImpl(
              EXECUTION_EXCEPTION, BeLittlingSeverity.ERROR, e.getMessage(), e.getCause()));
    } catch (TimeoutException e) {
      caller.addMessage(
          new BeLittlingMessageImpl(
              TIMEOUT,
              BeLittlingSeverity.ERROR,
              String.format("Operation timed out after %s %s", timeout, unit),
              e));
    }
    Instant stop = Instant.now();
    caller.addMessage(
        new BeLittlingMessageImpl(
            "WALL_CLOCK", BeLittlingSeverity.INFO, Duration.between(start, stop).toString()));
    return result;
  }

  /**
   * Destroy the executor instance. This effectively destroys the object.
   *
   * @return Any processes still running. Should always return an empty list.
   */
  public List<Runnable> shutdownNow() {
    return executor.shutdownNow();
  }

  /**
   * Implementation of AutoCloseable method. Shutdown object's resources.
   *
   * @throws Exception
   */
  @Override
  public void close() throws Exception {
    shutdownNow();
  }
}
