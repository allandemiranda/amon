package br.eti.allandemiranda.forex.utils;

import br.eti.allandemiranda.forex.exceptions.ThreadToolException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class Tools {

  private Tools() {
    throw new IllegalCallerException();
  }

  /**
   * Invert the array
   *
   * @param array The array
   * @return The inverted array
   */
  public static BigDecimal @NotNull [] invertArray(BigDecimal @NotNull [] array) {
    return IntStream.rangeClosed(1, array.length).mapToObj(i -> array[array.length - i]).toArray(BigDecimal[]::new);
  }

  /**
   * get the EMA vector
   *
   * @param period The period value
   * @param values The array with values
   * @return The EMA array
   */
  public static BigDecimal @NotNull [] getEMA(final int period, final BigDecimal @NotNull [] values) {
    final BigDecimal[] list = invertArray(values);
    final BigDecimal smaToFirstElement = Arrays.stream(list, 0, period).reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP);
    AtomicReference<BigDecimal> prevEMA = new AtomicReference<>(smaToFirstElement);
    final BigDecimal a = BigDecimal.TWO.divide(BigDecimal.valueOf(period + 1L), 10, RoundingMode.HALF_UP);
    final BigDecimal[] emaList = IntStream.range(period - 1, list.length).mapToObj(index -> {
      if (index == (period - 1)) {
        return prevEMA.get();
      } else {
        BigDecimal ema = (a.multiply(list[index])).add(BigDecimal.ONE.subtract(a).multiply(prevEMA.get()));
        prevEMA.set(ema);
        return ema;
      }
    }).toArray(BigDecimal[]::new);
    return invertArray(emaList);
  }

  /**
   * Tool to help on thread management
   *
   * @param threads The threads
   */
  public static void startThreadsUnstated(@NotNull Thread... threads) {
    Arrays.stream(threads).parallel().forEach(Thread::start);
    Arrays.stream(threads).forEachOrdered(thread -> {
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ThreadToolException(e);
      }
    });
  }

  /**
   * Convert the price in points
   *
   * @param price  The price
   * @param digits The number of digits
   * @return The number of points
   */
  public static int getPoints(final @NotNull BigDecimal price, final int digits) {
    return price.multiply(BigDecimal.valueOf(Math.pow(10, digits))).intValue();
  }
}
