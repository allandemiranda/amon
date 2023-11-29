package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.repositories.TradingPerformanceRepository;
import br.eti.allandemiranda.forex.utils.Tools;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class TradingPerformanceService {

  private final TradingPerformanceRepository repository;

  @Getter(AccessLevel.PUBLIC)
  @Value("${trading.parameters.exponential.period:9}")
  private int exponentialPeriod;
  @Getter(AccessLevel.PUBLIC)
  @Value("${trading.parameters.simple.period:21}")
  private int simplePeriod;
  @Getter(AccessLevel.PUBLIC)
  @Value("${candlestick.repository.memory:41}")
  private int memorySize;

  @Autowired
  public TradingPerformanceService(final TradingPerformanceRepository repository) {
    this.repository = repository;
  }

  /**
   * Add a new value of trading performance to the database
   *
   * @param simple      The simple value
   * @param exponential The exponential value
   */
  public void addTradingPerformance(final @NotNull BigDecimal simple, final @NotNull BigDecimal exponential) {
    this.getRepository().addTradingPerformance(simple, exponential);
  }

  /**
   * Get the current value in data base
   *
   * @return The pair of simple and exponential values
   */
  public SimpleEntry<BigDecimal, BigDecimal> getTradingPerformance() {
    return this.getRepository().getTradingPerformance();
  }

  /**
   * Positive diff between the simple and exponential value
   *
   * @param digits Digits of currency
   * @return The diff price in points
   */
  public int getDiff(final int digits) {
    return Tools.getPoints(this.getTradingPerformance().getKey().subtract(this.getTradingPerformance().getValue()).abs(), digits);
  }
}
