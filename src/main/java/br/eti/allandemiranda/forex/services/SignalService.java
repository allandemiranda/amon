package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.headers.SignalHeader;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
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

  private static @NotNull String getNumber(final double value) {
    return new DecimalFormat("#0.00000#").format(value).replace(".", ",");
  }

  public void addGlobalSignal(final @NotNull Signal signal) {
    this.getRepository().add(signal);
    this.updateDebugFile();
  }

  public boolean getValidation() {
    return this.getRepository().getValidation();
  }

  public boolean isReady() {
    return this.getRepository().isReady();
  }

  public Signal getLastSignal() {
    return this.getRepository().getLastSignal();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @PostConstruct
  private void init() {
    this.printDebugHeader();
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
  private void updateDebugFile() {
    if (this.isDebugActive() && this.getRepository().isReady()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        final Signal signal = this.getRepository().getLastSignal();
        csvPrinter.printRecord(signal.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), signal.trend(), getNumber(signal.price()));
      }
    }
  }
}
