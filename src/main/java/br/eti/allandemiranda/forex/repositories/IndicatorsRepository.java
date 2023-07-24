package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.controllers.indicators.SignalTrend;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import br.eti.allandemiranda.forex.headers.IndicatorsHeaders;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorsRepository {

  private final HashMap<String, SignalTrend> dataBase = new HashMap<>();

  @Value("${adx.repository.output}")
  private File outputFile;

  private @NotNull File getOutputFile() {
    return this.outputFile;
  }

  @PostConstruct
  public void init() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord((Object[]) IndicatorsHeaders.values());
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  public void updateFile(final @NotNull LocalDateTime dataTime) {
    String[] line = Arrays.stream(IndicatorsHeaders.values()).map(Enum::toString)
        .map(s -> IndicatorsHeaders.dataTime.toString().equals(s) ? dataTime.format(DateTimeFormatter.ISO_DATE_TIME) : dataBase.getOrDefault(s, SignalTrend.Out).toString())
        .toArray(String[]::new);
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord((Object) line);
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  public void add(final @NotNull String name, final @NotNull SignalTrend signal) {
    this.dataBase.put(name, signal);
  }
}
