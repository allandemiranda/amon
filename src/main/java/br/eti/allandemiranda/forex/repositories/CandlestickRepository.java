package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
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
public class CandlestickRepository {

  private final TreeSet<CandlestickEntity> dataBase = new TreeSet<>();

  @Value("${candlestick.repository.memory:15}")
  @Getter(AccessLevel.PUBLIC)
  private int memorySize;

  public boolean isReady() {
    return this.getDataBase().size() >= this.getMemorySize();
  }

  @Synchronized
  public void add(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime candlestickDateTime, final @NotNull BigDecimal price) {
    final CandlestickEntity entity = new CandlestickEntity();
    entity.setDateTime(candlestickDateTime);
    if (this.getDataBase().contains(entity)) {
      final CandlestickEntity older = this.getDataBase().first();
      older.setRealDateTime(realDataTime);
      older.setClose(price);
      if (older.getClose().compareTo(older.getHigh()) > 0) {
        older.setHigh(price);
      } else if (older.getClose().compareTo(older.getLow()) < 0) {
        older.setLow(price);
      }
    } else {
      entity.setRealDateTime(realDataTime);
      entity.setHigh(price);
      entity.setLow(price);
      entity.setOpen(price);
      entity.setClose(price);
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > this.getMemorySize()) {
        this.getDataBase().pollLast();
      }
    }
  }

  public Candlestick @NotNull [] get() {
    return this.getDataBase().stream().map(this::toModel).toArray(Candlestick[]::new);
  }

  private @NotNull Candlestick toModel(final @NotNull CandlestickEntity output) {
    return new Candlestick(output.getRealDateTime(), output.getDateTime(), output.getOpen(), output.getHigh(), output.getLow(), output.getClose());
  }

  public @NotNull Candlestick getLastUpdate() {
    return this.toModel(this.getDataBase().first());
  }
}
