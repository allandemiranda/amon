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

  @Synchronized
  public void add(final @NotNull LocalDateTime candleDataTime, final @NotNull SignalTrend trend) {
    this.setDataTime(candleDataTime);
    this.setTrend(trend);
  }

  public Signal get() {
    return new Signal(this.getDataTime(), this.getTrend());
  }
}
