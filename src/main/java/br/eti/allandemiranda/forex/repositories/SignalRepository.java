package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.entities.SignalEntity;
import br.eti.allandemiranda.forex.services.SignalService;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class SignalRepository {

  private final TreeSet<SignalEntity> dataBase = new TreeSet<>();

  @Value("${signal.repository.memory:3}")
  private int memorySize;

  @Synchronized
  public void add(final @NotNull Signal signal) {
    if (this.getDataBase().isEmpty() || signal.dateTime().isAfter(this.getDataBase().last().getDateTime())) {
      final SignalEntity entity = new SignalEntity();
      entity.setDateTime(signal.dateTime());
      entity.setTrend(signal.trend());
      entity.setPrice(signal.price());
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > this.getMemorySize()) {
        final SignalEntity older = this.getDataBase().first();
        this.getDataBase().remove(older);
      }
    } else {
      log.warn("Trying to add a old Signal on repository");
    }
  }

  public Signal @NotNull [] getSignals() {
    return this.getDataBase().stream().map(entity -> new Signal(entity.getDateTime(), entity.getTrend(), entity.getPrice())).toArray(Signal[]::new);
  }

  private @NotNull Signal toModel(final @NotNull SignalEntity entity) {
    return new Signal(entity.getDateTime(), entity.getTrend(), entity.getPrice());
  }

  public Signal getLastSignal() {
    try {
      return this.toModel(this.getDataBase().last());
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  public boolean isReady() {
    return this.getDataBase().size() >= this.getMemorySize();
  }
}
