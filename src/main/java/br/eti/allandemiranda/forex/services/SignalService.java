package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Signal;
import br.eti.allandemiranda.forex.headers.SignalHeader;
import br.eti.allandemiranda.forex.repositories.SignalRepository;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
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
public class SignalService {

  private static final String OUTPUT_FILE_NAME = "signals.csv";

  private final SignalRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${signal.debug:false}")
  private boolean debugActive;

  @Autowired
  private SignalService(final SignalRepository repository) {
    this.repository = repository;
  }

  public void addGlobalSignal(final @NotNull Signal signal) {
    this.getRepository().add(signal);
    this.updateDebugFile();
  }

  public Signal @NotNull [] getSignals() {
    return this.getRepository().getSignals();
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
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
        csvPrinter.printRecord(Arrays.stream(SignalHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile() {
    if (this.isDebugActive()) {
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
        final Signal signal = this.getRepository().getSignals()[this.getRepository().getSignals().length - 1];
        csvPrinter.printRecord(signal.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), signal.trend(), signal.price());
      }
    }
  }
}
