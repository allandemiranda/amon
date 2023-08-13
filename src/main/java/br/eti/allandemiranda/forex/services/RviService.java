package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.RVI;
import br.eti.allandemiranda.forex.headers.RviHeaders;
import br.eti.allandemiranda.forex.repositories.RviRepository;
import br.eti.allandemiranda.forex.utils.IndicatorTrend;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
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
public class RviService {

  private static final String OUTPUT_FILE_NAME = "rvi.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final RviRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${rvi.debug:true}")
  private boolean debugActive;

  @Autowired
  protected RviService(final RviRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public void addRvi(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime dataTime, final @NotNull BigDecimal rvi, final @NotNull BigDecimal signal) {
    this.getRepository().add(realDataTime, dataTime, rvi, signal);
  }

  public RVI[] getRVIs() {
    return this.getRepository().get();
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
        csvPrinter.printRecord(Arrays.stream(RviHeaders.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull LocalDateTime realTime, final @NotNull IndicatorTrend trend, final @NotNull BigDecimal price) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final RVI[] rvis = this.getRepository().get();
        final RVI rvi = rvis[rvis.length - 1];
        csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), rvi.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), getNumber(rvi.value()),
            getNumber(rvi.signal()), trend, getNumber(price));
      }
    }
  }

}
