package br.eti.allandemiranda.forex.repositories;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class TradingPerformanceRepository {

  private BigDecimal simple = BigDecimal.ZERO;
  private BigDecimal exponential = BigDecimal.ZERO;

  /**
   * Add a new value of trading performance to the database
   *
   * @param simple      The simple value
   * @param exponential The exponential value
   */
  public void addTradingPerformance(final @NotNull BigDecimal simple, final @NotNull BigDecimal exponential) {
    this.setSimple(simple);
    this.setExponential(exponential);
  }

  /**
   * Get the current value in data base
   *
   * @return The pair of simple and exponential values
   */
  public SimpleEntry<BigDecimal, BigDecimal> getTradingPerformance() {
    return new SimpleEntry<>(this.getSimple(), this.getExponential());
  }
}
