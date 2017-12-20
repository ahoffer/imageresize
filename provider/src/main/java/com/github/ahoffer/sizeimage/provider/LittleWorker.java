package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.EXECUTION_EXCEPTION;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.THREAD_INTERRUPTED;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.TIMEOUT;

import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.ImageSizer;
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

  protected ExecutorService executor;
  protected ImageSizer caller;
  protected long timeout;
  protected TimeUnit unit;

  public LittleWorker(long timeout, TimeUnit unit) {
    this(new NullImageSizer(), timeout, unit);
  }

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

  public void setTimeout(long timeout, TimeUnit unit) {
    this.timeout = timeout;
    this.unit = unit;
  }

  public <T> T doThis(Callable<T> supplier) {
    Future<T> future = executor.submit(supplier);
    T result = null;
    try {
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
    return result;
  }

  public List<Runnable> shutdownNow() {
    return executor.shutdownNow();
  }

  @Override
  public void close() throws Exception {
    shutdownNow();
  }
}
