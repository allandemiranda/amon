package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import br.eti.allandemiranda.forex.headers.CandlestickHeaders;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.TreeSet;
import lombok.Synchronized;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class CandlestickRepository implements DataRepository<CandlestickEntity> {

  @Value("${candlestick.repository.output}")
  private File outputFile;

  private TreeSet<CandlestickEntity> dataBase = new TreeSet<>();

  @Override
  @Synchronized
  public @NotNull Collection<CandlestickEntity> getDataBase() {
    return dataBase;
  }

  private File getOutputFile() {
    return this.outputFile;
  }

  @Override
  @PostConstruct
  public void initDataBase() {
    dataBase = new TreeSet<>();
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord("realDateTime", "dateTime", CandlestickHeaders.open, CandlestickHeaders.high, CandlestickHeaders.low, CandlestickHeaders.close);
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  public void saveRunTime(final @NotNull CandlestickEntity candlestick, final @NotNull LocalDateTime realTime) {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), candlestick.getDateTime().format(DateTimeFormatter.ISO_DATE_TIME),
          String.valueOf(candlestick.getOpen()).replace(".", ","), String.valueOf(candlestick.getHigh()).replace(".", ","), String.valueOf(candlestick.getLow()).replace(".", ","),
          String.valueOf(candlestick.getClose()).replace(".", ","));
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  @Override
  public @NotNull TreeSet<CandlestickEntity> selectAll() {
    return new TreeSet<>(DataRepository.super.selectAll());
  }


}
