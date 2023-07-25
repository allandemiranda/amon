package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import br.eti.allandemiranda.forex.entities.ADXEntity;
import br.eti.allandemiranda.forex.headers.ADXHeaders;
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
public class ADXRepository implements DataRepository<ADXEntity>, SaveRunTimeRepository {

  private final Collection<ADXEntity> collection = new ArrayList<>();

  @Value("${adx.repository.output}")
  private File outputFile;

  private static @NotNull String getStringNumber(double number) {
    return String.valueOf(number).replace(".", ",");
  }

  @Override
  @Synchronized
  public @NotNull Collection<ADXEntity> getDataBase() {
    return this.collection;
  }

  @Override
  public long getMemorySide() {
    return 2L;
  }

  @Override
  public @NotNull File getOutputFile() {
    return this.outputFile;
  }

  @PostConstruct
  public void init() {
    saveHeaders();
  }

  @Override
  public Object[] getHeaders() {
    return new Object[]{ADXHeaders.realDateTime, ADXHeaders.candleDateTime, ADXHeaders.adx, ADXHeaders.diPlus, ADXHeaders.diMinus, ADXHeaders.trend, ADXHeaders.price};
  }

  @Override
  public Object[] getLine(Object @NotNull ... inputs) {
    LocalDateTime realDateTime = (LocalDateTime) inputs[0];
    ADXEntity adx = (ADXEntity) inputs[1];
    SignalTrend trend = (SignalTrend) inputs[2];
    double price = (Double) inputs[3];
    return new Object[]{realDateTime.format(DateTimeFormatter.ISO_DATE_TIME), adx.getDateTime().format(DateTimeFormatter.ISO_DATE_TIME), getStringNumber(adx.getAdx()),
        getStringNumber(adx.getDiPlus()), getStringNumber(adx.getDiMinus()), trend, getStringNumber(price)};
  }
}
