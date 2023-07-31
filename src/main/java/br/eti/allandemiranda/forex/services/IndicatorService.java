package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.controllers.indicators.Indicator;
import br.eti.allandemiranda.forex.repositories.IndicatorRepository;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class IndicatorService {

  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  private static final String OUTPUT_FILE_NAME = "indicators.csv";
  private static final String DATA_TIME = "DATA_TIME";
  private static final String PRICE = "PRICE";
  private final IndicatorRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${indicators.debug:false}")
  private boolean debugActive;
  @Setter(AccessLevel.PRIVATE)
  private String[] header = null;

  @Autowired
  protected IndicatorService(final IndicatorRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final double value) {
    return new DecimalFormat("#0.0000#").format(value).replace(".", ",");
  }

  public void addIndicator(final @NotNull String name, final Indicator indicator) {
    this.getRepository().add(name, indicator);
  }

  public @NotNull Map<String, SignalTrend> processAndGetSignals(final @NotNull LocalDateTime dataTime) {
    return this.getRepository().processAndGetSignals(dataTime);
  }

  public @NotNull LocalDateTime getLastUpdate() {
    return this.getRepository().getLastUpdate();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      this.header = Stream.concat(Stream.of(DATA_TIME, PRICE), this.getRepository().getNames().stream()).toArray(String[]::new);
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(this.getHeader()).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull Map<String, SignalTrend> signalsMap, final double price) {
    if (this.isDebugActive()) {
      if (Objects.isNull(this.getHeader())) {
        this.setHeader(Stream.concat(Stream.of(DATA_TIME, PRICE), this.getRepository().getNames().stream()).toArray(String[]::new));
        this.printDebugHeader();
      }
      final Object[] row = Arrays.stream(this.getHeader()).map(s -> {
        if (DATA_TIME.equals(s)) {
          return this.getLastUpdate().format(DateTimeFormatter.ISO_DATE_TIME);
        } else if (PRICE.equals(s)) {
          return getNumber(price);
        } else {
          return signalsMap.getOrDefault(s, SignalTrend.OUT);
        }
      }).toArray();
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(row);
      }
    }
  }
}
