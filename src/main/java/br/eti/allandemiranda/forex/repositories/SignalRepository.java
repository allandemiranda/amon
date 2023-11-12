package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class SignalRepository {

  private LocalDateTime dataTime = LocalDateTime.MIN;
  private SignalTrend trend = SignalTrend.NEUTRAL;

  /**
   * Add new trend information
   *
   * @param candleDataTime The last close candlestick time
   * @param trend          The signal trend of signals from indicators
   */
  @Synchronized
  public void add(final @NotNull LocalDateTime candleDataTime, final @NotNull SignalTrend trend) {
    this.setDataTime(candleDataTime);
    this.setTrend(trend);
  }

  /**
   * Get last signal trend
   *
   * @return The global signal trend
   */
  public Signal get() {
    return new Signal(this.getDataTime(), this.getTrend());
  }
}
