package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.MACD;
import br.eti.allandemiranda.forex.entities.MacdEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class MacdRepository {

  private static final int MEMORY_SIZE = 3;
  private final TreeSet<MacdEntity> dataBase = new TreeSet<>();

  public void add(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal macd, final @NotNull BigDecimal signal) {
    if (this.getDataBase().isEmpty() || dateTime.isAfter(this.getDataBase().first().getDateTime())) {
      final MacdEntity entity = new MacdEntity();
      entity.setRealDateTime(realDataTime);
      entity.setDateTime(dateTime);
      entity.setMain(macd);
      entity.setSignal(signal);
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > MEMORY_SIZE) {
        this.getDataBase().pollLast();
      }
    } else {
      this.getDataBase().first().setRealDateTime(realDataTime);
      this.getDataBase().first().setMain(macd);
      this.getDataBase().first().setSignal(signal);
    }
  }

  public MACD @NotNull [] get() {
    return this.getDataBase().stream().map(this::toModel).toArray(MACD[]::new);
  }

  private @NotNull MACD toModel(final @NotNull MacdEntity entity) {
    return new MACD(entity.getRealDateTime(), entity.getDateTime(), entity.getMain(), entity.getSignal());
  }
}
