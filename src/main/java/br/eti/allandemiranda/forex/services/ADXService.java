package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.ADX;
import br.eti.allandemiranda.forex.headers.ADXHeaders;
import br.eti.allandemiranda.forex.repositories.ADXRepository;
import br.eti.allandemiranda.forex.utils.SignalTrend;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
public class ADXService {

  private static final String OUTPUT_FILE_NAME = "adx.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final ADXRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${adx.debug:false}")
  private boolean debugActive;

  @Autowired
  protected ADXService(final ADXRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final double value) {
    return new DecimalFormat("#0.00000#").format(value).replace(".", ",");
  }

  public void addADX(final @NotNull LocalDateTime candlestickTime, final double adx, final double diPlus, final double diMinus) {
    this.getRepository().add(candlestickTime, adx, diPlus, diMinus);
  }

  public @NotNull ADX getADX() {
    return this.getRepository().getADX();
  }

  @PostConstruct
  private void init() {
    this.printDebugHeader();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @SneakyThrows
  private void printDebugHeader() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(ADXHeaders.values()).map(Enum::toString).toArray());
      }
    }
  }

  public void updateDebugFile(final @NotNull LocalDateTime realTime, final @NotNull SignalTrend trend, final double price) {
    this.updateDebugFile(realTime, this.getRepository(), trend, price);
  }

  @SneakyThrows
  private void updateDebugFile(final @NotNull LocalDateTime realTime, final @NotNull ADXRepository repository, final @NotNull SignalTrend trend, final double price) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final ADX adx = repository.getADX();
        csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), adx.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), getNumber(adx.value()),
            getNumber(adx.diPlus()), getNumber(adx.diMinus()), trend, getNumber(price));
      }
    }
  }
}
