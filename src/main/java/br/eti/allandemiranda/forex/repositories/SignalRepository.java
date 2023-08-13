package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.entities.SignalEntity;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeSet;
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
  public void add(final @NotNull LocalDateTime dateTime, final @NotNull SignalTrend trend, final @NotNull BigDecimal price) {
    final SignalEntity entity = new SignalEntity();
    entity.setDateTime(dateTime);
    entity.setTrend(trend);
    entity.setPrice(price);
    this.getDataBase().add(entity);
    if (this.getDataBase().size() > this.getOpenWith()) {
      this.getDataBase().pollFirst();
    }
  }

  public Signal[] get() {
    return this.getDataBase().stream().map(this::toModel).toArray(Signal[]::new);
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
