package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.headers.SignalHeader;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.SortedMap;
import lombok.AccessLevel;
import lombok.Getter;
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

  @Autowired
  protected SignalService(final SignalRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public void addGlobalSignal(final @NotNull LocalDateTime candleDataTime, final @NotNull SignalTrend globalSignal) {
    this.getRepository().add(candleDataTime, globalSignal);
  }

  public Signal getLastSignal() {
    return this.getRepository().get();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @PostConstruct
  private void init() {
    printDebugHeader();
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(SignalHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull Candlestick candlestick, final @NotNull SortedMap<String, IndicatorTrend> indicators) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final Signal signal = this.getRepository().get();
        final String indicatorsBuy = Arrays.toString(
            indicators.entrySet().stream().filter(entry -> entry.getValue().equals(IndicatorTrend.BUY)).map(Entry::getKey).toArray());
        final String indicatorsSell = Arrays.toString(
            indicators.entrySet().stream().filter(entry -> entry.getValue().equals(IndicatorTrend.SELL)).map(Entry::getKey).toArray());
        final String indicatorsNeutral = Arrays.toString(
            indicators.entrySet().stream().filter(entry -> entry.getValue().equals(IndicatorTrend.NEUTRAL)).map(Entry::getKey).toArray());
        final String empty = "";
        if (signal.trend().equals(SignalTrend.STRONG_BUY)) {
          csvPrinter.printRecord(candlestick.dateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), getNumber(candlestick.open()), getNumber(candlestick.high()),
              getNumber(candlestick.low()), getNumber(candlestick.close()), getNumber(candlestick.close()), empty, empty, indicatorsBuy, indicatorsSell,
              indicatorsNeutral);
        } else if (signal.trend().equals(SignalTrend.STRONG_SELL)) {
          csvPrinter.printRecord(candlestick.dateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), getNumber(candlestick.open()), getNumber(candlestick.high()),
              getNumber(candlestick.low()), getNumber(candlestick.close()), empty, getNumber(candlestick.close()), empty, indicatorsBuy, indicatorsSell,
              indicatorsNeutral);
        } else if (signal.trend().equals(SignalTrend.NEUTRAL)) {
          csvPrinter.printRecord(candlestick.dateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), getNumber(candlestick.open()), getNumber(candlestick.high()),
              getNumber(candlestick.low()), getNumber(candlestick.close()), empty, empty, getNumber(candlestick.close()), indicatorsBuy, indicatorsSell,
              indicatorsNeutral);
        }
      }
    }
  }
}
