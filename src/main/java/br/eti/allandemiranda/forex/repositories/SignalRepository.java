package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.entities.SignalEntity;
import java.time.LocalDateTime;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class SignalRepository {

  private final TreeSet<SignalEntity> dataBase = new TreeSet<>();

  @Value("${signal.repository.memory}")
  private int memorySize;

  public int getCacheSize() {
    return dataBase.size();
  }

  @Synchronized
  public void add(final @NotNull Signal signal) {
    if (this.getCacheSize() == 0 || signal.dateTime().isAfter(this.getDataBase().last().getDateTime())) {
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

  public LocalDateTime getLastUpdateTime() {
    return this.getDataBase().stream().map(SignalEntity::getDateTime).findFirst().orElse(LocalDateTime.MIN);
  }
}
