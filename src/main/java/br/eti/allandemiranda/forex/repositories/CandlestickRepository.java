package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TreeSet;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
public class CandlestickRepository {

  private static final int SKIP_CURRENT_CANDLESTICK = 1;
  private final TreeSet<CandlestickEntity> dataBase = new TreeSet<>();

  @Value("${candlestick.repository.memory:41}")
  @Getter(AccessLevel.PUBLIC)
  private int memorySize;

  public boolean isReady() {
    return this.getDataBase().size() >= this.getMemorySize();
  }

  @Synchronized
  public void add(final @NotNull LocalDateTime candlestickDateTime, final @NotNull BigDecimal price) {
    final CandlestickEntity entity = new CandlestickEntity();
    entity.setDateTime(candlestickDateTime);
    if (this.getDataBase().contains(entity)) {
      final CandlestickEntity older = this.getDataBase().first();
      older.setClose(price);
      if (older.getClose().compareTo(older.getHigh()) > 0) {
        older.setHigh(price);
      } else if (older.getClose().compareTo(older.getLow()) < 0) {
        older.setLow(price);
      }
    } else {
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

  public Stream<Candlestick> get(final int size) {
    return this.getDataBase().stream().skip(SKIP_CURRENT_CANDLESTICK).limit(size).map(this::toModel);
  }

  private @NotNull Candlestick toModel(final @NotNull CandlestickEntity output) {
    return new Candlestick(output.getDateTime(), output.getOpen(), output.getHigh(), output.getLow(), output.getClose());
  }

  public @NotNull Candlestick getLastUpdate() {
    return this.toModel(this.getDataBase().first());
  }
}
