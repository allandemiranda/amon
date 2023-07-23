package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.headers.CandlestickHeaders;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.TreeSet;
import lombok.Synchronized;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class CandlestickRepository implements LoadFileRepository<CandlestickEntity> {

  private final TreeSet<CandlestickEntity> dataBase = new TreeSet<>();

  @Value("${candlestick.repository.input}")
  private File inputFile;

  @Override
//  @Synchronized
  public @NotNull Collection<CandlestickEntity> getDataBase() {
    return dataBase;
  }

  @Override
  @PostConstruct
  public void initDataBase() {
    this.loadData().forEach(this::addData);
  }

  @Override
  public @NotNull File getInputFile() {
    return this.inputFile;
  }

  @Override
  public @NotNull CSVFormat getInputCsvFormat() {
    return CSVFormat.TDF.builder().setHeader(CandlestickHeaders.class).setSkipHeaderRecord(true).build();
  }

  @Override
  public @NotNull CandlestickEntity getEntity(@NotNull CSVRecord csvRecord) {
    CandlestickEntity entity = new CandlestickEntity();

    String date = csvRecord.get(CandlestickHeaders.date);
    String time = csvRecord.get(CandlestickHeaders.time);
    String dataTime = date.replace(".", "-").concat("T").concat(time);
    entity.setDateTime(LocalDateTime.parse(dataTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    entity.setOpen(Double.parseDouble(csvRecord.get(CandlestickHeaders.open)));
    entity.setHigh(Double.parseDouble(csvRecord.get(CandlestickHeaders.high)));
    entity.setLow(Double.parseDouble(csvRecord.get(CandlestickHeaders.low)));
    entity.setClose(Double.parseDouble(csvRecord.get(CandlestickHeaders.close)));
    return entity;
  }
}
