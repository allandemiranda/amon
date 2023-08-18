package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.DeM;
import br.eti.allandemiranda.forex.headers.DemHeader;
import br.eti.allandemiranda.forex.repositories.DemRepository;
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
public class DemService {

  private static final String OUTPUT_FILE_NAME = "dem.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final DemRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${dem.debug:false}")
  private boolean debugActive;

  @Autowired
  protected DemService(final DemRepository repository) {
    this.repository = repository;
  }

  private static @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.00000#").format(value.doubleValue()).replace(".", ",");
  }

  public void addDem(final @NotNull LocalDateTime dataTime, final @NotNull BigDecimal dMark) {
    this.getRepository().add(dataTime, dMark);
  }

  public @NotNull DeM getDem() {
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
        csvPrinter.printRecord(Arrays.stream(DemHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull LocalDateTime realTime, final @NotNull IndicatorTrend trend, final @NotNull BigDecimal price) {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final DeM deM = this.getRepository().get();
        csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), deM.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), getNumber(deM.value()), trend,
            getNumber(price));
      }
    }
  }
}
