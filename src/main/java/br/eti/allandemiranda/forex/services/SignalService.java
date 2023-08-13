package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.headers.SignalHeader;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.stream.Collectors;
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
public class SignalService {

  private static final String OUTPUT_FILE_NAME = "signals.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final SignalRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${signal.debug:false}")
  private boolean debugActive;
  @Setter(AccessLevel.PRIVATE)
  private boolean isPrinted = false;

  @Autowired
  protected SignalService(final SignalRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public void addGlobalSignal(final @NotNull Candlestick candlestick, final @NotNull SignalTrend globalSignal, final @NotNull SortedMap<String, IndicatorTrend> signals) {
    this.getRepository().add(candlestick.realDateTime(), globalSignal, candlestick.close());
    this.updateDebugFile(signals);
  }

  public boolean isOpenSignal() {
    if (this.getRepository().isReady()) {
      return Arrays.stream(this.getRepository().get()).map(Signal::trend).collect(Collectors.toCollection(HashSet::new)).size() == 1;
    } else {
      return false;
    }
  }

  public Signal getOpenSignal() {
    return this.getRepository().getLastSignal();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @SneakyThrows
  private void updateDebugFile(final @NotNull SortedMap<String, IndicatorTrend> signals) {
    if (this.isDebugActive() && this.getRepository().isReady()) {
      if (!this.isPrinted()) {
        try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
          csvPrinter.printRecord(Stream.concat(Arrays.stream(SignalHeader.values()), signals.keySet().stream()).toArray());
        }
        this.setPrinted(true);
      }
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final Signal signal = this.getRepository().getLastSignal();
        csvPrinter.printRecord(Stream.concat(Stream.of(signal.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), signal.trend(), getNumber(signal.price())),
            signals.values().stream().map(Enum::toString)).toArray());
      }
    }
  }
}
