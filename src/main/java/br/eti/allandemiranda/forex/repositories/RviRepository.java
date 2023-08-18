package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.RVI;
import br.eti.allandemiranda.forex.entities.RviEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class RviRepository {

  private static final int MEMORY_SIZE = 3;
  private final TreeSet<RviEntity> dataBase = new TreeSet<>();

  public void add(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal rvi, final @NotNull BigDecimal signal) {
    if (this.getDataBase().isEmpty() || dateTime.isAfter(this.getDataBase().first().getDateTime())) {
      final RviEntity entity = new RviEntity();
      entity.setRealDateTime(realDataTime);
      entity.setDateTime(dateTime);
      entity.setValue(rvi);
      entity.setSignal(signal);
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > MEMORY_SIZE) {
        this.getDataBase().pollLast();
      }
    } else {
      this.getDataBase().first().setRealDateTime(realDataTime);
      this.getDataBase().first().setValue(rvi);
      this.getDataBase().first().setSignal(signal);
    }
  }

  public RVI @NotNull [] get() {
    return this.getDataBase().stream().map(this::toModel).toArray(RVI[]::new);
  }

  private @NotNull RVI toModel(final @NotNull RviEntity entity) {
    return new RVI(entity.getRealDateTime(), entity.getDateTime(), entity.getValue(), entity.getSignal());
  }
}
