package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.headers.CandlestickHeader;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
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
public class CandlestickService {

  private static final String OUTPUT_FILE_NAME = "candlestick.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();

  private final CandlestickRepository repository;

  @Value("${config.root.folder}")
  private File outputFolder;
  @Value("${candlestick.debug:false}")
  private boolean debugActive;

  @Autowired
  protected CandlestickService(final CandlestickRepository repository) {
    this.repository = repository;
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
        csvPrinter.printRecord(Arrays.stream(CandlestickHeader.values()).map(Enum::toString).toArray());
      }
    }
  }

  @SneakyThrows
  public void updateDebugFile(final @NotNull LocalDateTime realTime) {
    if (this.isDebugActive()) {
      final Candlestick candlestick = this.getRepository().getCandlesticks()[this.getRepository().getCacheSize() - 1];
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(realTime.format(DateTimeFormatter.ISO_DATE_TIME), candlestick.dateTime().format(DateTimeFormatter.ISO_DATE_TIME), candlestick.open(),
            candlestick.high(), candlestick.low(), candlestick.close());
      }
    }
  }

  public void addTicket(final @NotNull Ticket ticket) {
    final double price = ticket.bid();
    final Candlestick candlestick = new Candlestick(ticket.dateTime(), price, price, price, price);
    this.getRepository().addCandlestick(candlestick);
  }
}
