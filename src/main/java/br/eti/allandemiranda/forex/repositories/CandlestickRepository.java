package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
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
public class CandlestickRepository {

  private final TreeSet<CandlestickEntity> dataBase = new TreeSet<>();

  @Value("${candlestick.repository.memory}")
  private int memorySize;

  public int getCacheSize() {
    return this.getDataBase().size();
  }

  @Synchronized
  public void addCandlestick(final @NotNull Candlestick candlestick) {
    final LocalDateTime candlestickDataTime = candlestick.dateTime();
    final CandlestickEntity lastCandlestick = this.getDataBase().last();
    if (this.getCacheSize() == 0 || candlestickDataTime.isAfter(lastCandlestick.getDateTime())) {
      final CandlestickEntity entity = new CandlestickEntity();
      entity.setDateTime(candlestickDataTime);
      entity.setOpen(candlestick.open());
      this.getDataBase().add(entity);
      if (this.getDataBase().size() > this.getMemorySize()) {
        final CandlestickEntity older = this.getDataBase().first();
        this.getDataBase().remove(older);
      }
    } else if (lastCandlestick.getDateTime().equals(candlestickDataTime)) {
      lastCandlestick.setClose(candlestick.close());
    } else {
      log.warn("Trying to add a old Candlestick on repository");
    }
  }

  public Candlestick @NotNull [] getCandlesticks() {
    return this.getDataBase().stream().map(this::toModel).toArray(Candlestick[]::new);
  }

  private @NotNull Candlestick toModel(final @NotNull CandlestickEntity output) {
    return new Candlestick(output.getDateTime(), output.getOpen(), output.getHigh(), output.getLow(), output.getClose());
  }

  public LocalDateTime getLastDataTime() {
    if (this.getDataBase().isEmpty()) {
      return LocalDateTime.MIN;
    } else {
      return this.getDataBase().last().getDateTime();
    }
  }
}
