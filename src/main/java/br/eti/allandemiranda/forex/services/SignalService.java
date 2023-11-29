package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.enums.SignalTrend;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class SignalService {

  private final SignalRepository repository;

  @Autowired
  protected SignalService(final SignalRepository repository) {
    this.repository = repository;
  }

  /**
   * Add new global trend information
   *
   * @param candleDataTime The last close candlestick time
   * @param globalSignal   The signal trend of signals from indicators
   */
  public void addGlobalSignal(final @NotNull LocalDateTime candleDataTime, final @NotNull SignalTrend globalSignal) {
    this.getRepository().add(candleDataTime, globalSignal);
  }

  /**
   * Get last global signal trend
   *
   * @return The global signal trend
   */
  public Signal getLastSignal() {
    return this.getRepository().get();
  }

}
