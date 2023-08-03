package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.exceptions.ServiceException;
import br.eti.allandemiranda.forex.headers.CandlestickHeader;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
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

  private static @NotNull String getNumber(final double value) {
    return new DecimalFormat("#0.00000#").format(value).replace(".", ",");
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
  public void updateDebugFile() {
    if (this.isDebugActive()) {
      final Candlestick candlestick = this.getRepository().getLastCandlestick();
      try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
        csvPrinter.printRecord(candlestick.realDateTime().format(DateTimeFormatter.ISO_DATE_TIME), candlestick.dateTime().format(DateTimeFormatter.ISO_DATE_TIME),
            getNumber(candlestick.open()), getNumber(candlestick.high()), getNumber(candlestick.low()), getNumber(candlestick.close()));
      }
    }
  }

  public void addTicket(final @NotNull Ticket ticket, final @NotNull LocalDateTime candlestickDateTime) {
    this.getRepository().addCandlestick(ticket.dateTime(), candlestickDateTime, ticket.bid());
    this.updateDebugFile();
  }

  public Candlestick @NotNull [] getCandlesticks(final int periodNum) {
    if (this.getRepository().getMemorySize() >= periodNum) {
      return Arrays.stream(this.getRepository().getCandlesticks(), this.getRepository().getMemorySize() - periodNum, this.getRepository().getMemorySize())
          .toArray(Candlestick[]::new);
    }
    throw new ServiceException("Can't provide Candlesticks to the period requested");
  }

  public boolean isReady() {
    return this.getRepository().isReady();
  }

  public Candlestick getLastCandlestick() {
    return this.getRepository().getLastCandlestick();
  }
}
