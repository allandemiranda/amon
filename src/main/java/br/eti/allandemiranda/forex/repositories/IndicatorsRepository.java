package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.utils.SignalTrend;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import lombok.Synchronized;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorsRepository {

  private static final String DATA_TIME = "dataTime";
  private static final String PRICE = "price";
  private static final String TARGET = ".";
  private static final String REPLACEMENT = ",";

  private final HashMap<String, SignalTrend> dataBase = new HashMap<>();

  @Value("${indicators.repository.output}")
  private File outputFile;
  private String[] headers = null;

  private @NotNull File getOutputFile() {
    return this.outputFile;
  }

  public void printHeaders(final @NotNull Object... inputs) {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(inputs);
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
    this.headers = Arrays.stream(inputs).map(o -> (String) o).toArray(String[]::new);
  }

  public void updateFile(final @NotNull LocalDateTime dataTime, final double price) {
    Object[] line = Arrays.stream(this.headers).map(s -> {
      if (DATA_TIME.equals(s)) {
        return dataTime.format(DateTimeFormatter.ISO_DATE_TIME);
      }
      if (PRICE.equals(s)) {
        return String.valueOf(price).replace(TARGET, REPLACEMENT);
      }
      return dataBase.getOrDefault(s, SignalTrend.out).toString();
    }).toArray();
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(line);
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  @Synchronized
  public void add(final @NotNull String name, final @NotNull SignalTrend signal) {
    this.dataBase.put(name, signal);
  }
}
