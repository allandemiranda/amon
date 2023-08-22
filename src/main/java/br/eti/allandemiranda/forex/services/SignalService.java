package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import br.eti.allandemiranda.forex.utils.SignalTrend;
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

  public void addGlobalSignal(final @NotNull LocalDateTime candleDataTime, final @NotNull SignalTrend globalSignal) {
    this.getRepository().add(candleDataTime, globalSignal);
  }

  public Signal getLastSignal() {
    return this.getRepository().get();
  }

}
