package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.entities.SignalEntity;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class SignalRepository {

  private final TreeSet<SignalEntity> dataBase = new TreeSet<>();

  @Value("${order.open.signals:3}")
  private int openWith;

  @Synchronized
  public void add(final @NotNull Signal signal) {
    final SignalEntity entity = new SignalEntity();
    entity.setDateTime(signal.dateTime());
    entity.setTrend(signal.trend());
    entity.setPrice(signal.price());
    this.getDataBase().add(entity);
    if (this.getDataBase().size() > this.getOpenWith()) {
      final SignalEntity older = this.getDataBase().first();
      this.getDataBase().remove(older);
    }
  }

  public boolean haveValidSignal() {
    return this.getDataBase().stream().map(SignalEntity::getTrend).collect(Collectors.toCollection(HashSet::new)).size() == 1;
  }

  private @NotNull Signal toModel(final @NotNull SignalEntity entity) {
    return new Signal(entity.getDateTime(), entity.getTrend(), entity.getPrice());
  }

  public @NotNull Signal getLastSignal() {
    return this.toModel(this.getDataBase().last());
  }

  public boolean isReady() {
    return this.getDataBase().size() >= this.getOpenWith();
  }
}
