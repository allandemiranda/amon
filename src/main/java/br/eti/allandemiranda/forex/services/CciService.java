package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.CCI;
import br.eti.allandemiranda.forex.headers.CciHeaders;
import br.eti.allandemiranda.forex.repositories.CciRepository;
import br.eti.allandemiranda.forex.utils.SignalTrend;
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
public class CciService {

  private static final String OUTPUT_FILE_NAME = "cci.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final CciRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${cci.debug:false}")
  private boolean debugActive;

  @Autowired
  protected CciService(final CciRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public void addCci(final @NotNull LocalDateTime dataTime, final @NotNull BigDecimal cci) {
    this.getRepository().add(dataTime, cci);
  }

  public @NotNull CCI getCci() {
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
        csvPrinter.printRecord(Arrays.stream(CciHeaders.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull LocalDateTime realTime, final @NotNull SignalTrend trend, final @NotNull BigDecimal price) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final CCI cci = this.getRepository().get();
        csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), cci.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), getNumber(cci.value()), trend,
            getNumber(price));
      }
    }
  }
}
