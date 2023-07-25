package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.headers.CandlestickHeaders;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class CandlestickRepository implements DataRepository<CandlestickEntity>, SaveRunTimeRepository {

  private static final String TARGET = ".";
  private static final String REPLACEMENT = ",";

  private final Collection<CandlestickEntity> collection = new ArrayList<>();

  @Value("${candlestick.repository.output}")
  private File outputFile;
  @Value("${candlestick.repository.memory}")
  private Integer memorySize;

  private static @NotNull String getStringNumber(double number) {
    return String.valueOf(number).replace(TARGET, REPLACEMENT);
  }

  @Override
  @Synchronized
  public @NotNull Collection<CandlestickEntity> getDataBase() {
    return collection;
  }

  @Override
  public long getMemorySide() {
    return this.memorySize;
  }

  @Override
  public File getOutputFile() {
    return this.outputFile;
  }

  @PostConstruct
  public void init() {
    saveHeaders();
  }

  @Override
  public Object[] getHeaders() {
    return new Object[]{CandlestickHeaders.realDateTime, CandlestickHeaders.candleDateTime, CandlestickHeaders.open, CandlestickHeaders.high, CandlestickHeaders.low,
        CandlestickHeaders.close};
  }

  @Override
  public Object[] getLine(Object @NotNull ... inputs) {
    LocalDateTime realDateTime = (LocalDateTime) inputs[1];
    CandlestickEntity candlestick = (CandlestickEntity) inputs[0];
    return new Object[]{realDateTime.format(DateTimeFormatter.ISO_DATE_TIME), candlestick.getDateTime().format(DateTimeFormatter.ISO_DATE_TIME),
        getStringNumber(candlestick.getOpen()), getStringNumber(candlestick.getHigh()), getStringNumber(candlestick.getLow()), getStringNumber(candlestick.getClose())};
  }
}
