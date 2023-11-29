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

  /**
   * Add the indicator to the repository
   *
   * @param dateTime The data time generated
   * @param macd     The MACD value
   * @param signal   The MACD signal
   */
  public void add(final @NotNull LocalDateTime dateTime, final @NotNull BigDecimal macd, final @NotNull BigDecimal signal) {
    if (this.getDataBase().isEmpty() || dateTime.isAfter(this.getDataBase().first().getDateTime())) {
      final MacdEntity entity = new MacdEntity();
      entity.setDateTime(dateTime);
      entity.setMain(macd);
      entity.setSignal(signal);
      if (!this.getDataBase().add(entity)) {
        this.getDataBase().remove(entity);
        this.getDataBase().add(entity);
      }
      if (this.getDataBase().size() > MEMORY_SIZE) {
        this.getDataBase().pollLast();
      }
    } else {
      this.getDataBase().first().setMain(macd);
      this.getDataBase().first().setSignal(signal);
    }
  }

  /**
   * Get the array of MACD
   *
   * @return The Array of MACD
   */
  public MACD @NotNull [] get() {
    return this.getDataBase().stream().map(this::toModel).toArray(MACD[]::new);
  }

  private @NotNull MACD toModel(final @NotNull MacdEntity entity) {
    return new MACD(entity.getDateTime(), entity.getMain(), entity.getSignal());
  }
}
