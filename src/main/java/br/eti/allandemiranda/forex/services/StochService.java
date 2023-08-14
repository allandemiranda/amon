package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.STOCH;
import br.eti.allandemiranda.forex.headers.StochHeaders;
import br.eti.allandemiranda.forex.repositories.StochRepository;
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
public class StochService {

  private static final String OUTPUT_FILE_NAME = "stoch.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final StochRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${stoch.debug:true}")
  private boolean debugActive;

  @Autowired
  protected StochService(final StochRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public void addStoch(final @NotNull LocalDateTime realDataTime, final @NotNull LocalDateTime dataTime, final @NotNull BigDecimal k, final @NotNull BigDecimal d) {
    this.getRepository().add(realDataTime, dataTime, k, d);
  }

  public STOCH @NotNull [] getStoch() {
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
        csvPrinter.printRecord(Arrays.stream(StochHeaders.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull LocalDateTime realTime, final @NotNull IndicatorTrend trend, final @NotNull BigDecimal price) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final STOCH stoch = this.getRepository().get()[0];
        csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), stoch.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), getNumber(stoch.main()),
            getNumber(stoch.signal()), trend, getNumber(price));
      }
    }
  }
}
