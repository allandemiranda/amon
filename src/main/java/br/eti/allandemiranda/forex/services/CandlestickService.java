package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandlestickService {

  private final CandlestickRepository candlestickRepository;

  @Autowired
  private CandlestickService(final CandlestickRepository candlestickRepository) {
    this.candlestickRepository = candlestickRepository;
  }

  private static @NotNull CandlestickEntity toEntity(@NotNull Candlestick model) {
    CandlestickEntity entity = new CandlestickEntity();
    entity.setDateTime(model.dateTime());
    entity.setLow(model.low());
    entity.setOpen(model.open());
    entity.setHigh(model.high());
    entity.setClose(model.close());
    return entity;
  }

  private static @NotNull Candlestick toModel(@NotNull CandlestickEntity entity) {
    return new Candlestick(entity.getDateTime(), entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose());
  }

  public void updateFile(final @NotNull LocalDateTime realDataTime) {
    this.candlestickRepository.saveRunTimeLine(this.candlestickRepository.getLast(), realDataTime);
  }

  public @NotNull Candlestick getLastCandlestick() {
    return toModel(this.candlestickRepository.getLast());
  }

  public double getLastCloseValue() {
    return this.candlestickRepository.getLast().getClose();
  }

  public long getCurrentMemorySize() {
    return this.candlestickRepository.selectAll().size();
  }

  public void add(final @NotNull Candlestick candlestick) {
    this.candlestickRepository.addData(toEntity(candlestick));
  }

  public double[] getCloseReversed(final int period) {
    return this.candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period)
        .mapToDouble(CandlestickEntity::getClose).toArray();
  }

  public double[] getHighReversed(final int period) {
    return this.candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period)
        .mapToDouble(CandlestickEntity::getHigh).toArray();
  }

  public double[] getLowReversed(final int period) {
    return this.candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period)
        .mapToDouble(CandlestickEntity::getLow).toArray();
  }
}
