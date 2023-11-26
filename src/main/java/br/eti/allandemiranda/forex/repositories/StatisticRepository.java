package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.headers.OrderHeader;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.utils.TimeFrame;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class StatisticRepository {
  //! This is a temporary class to generate temporary statistic values for performance of results
  //! This class needs to be removed at the end of this project

  private static final String OUTPUT_FILE_NAME = "statistic.csv";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  private final CandlestickService candlestickService;
  private final TreeMap<DayOfWeek, TreeMap<LocalTime, Pair<AtomicInteger, AtomicInteger>>> dataBase = new TreeMap<>();
  @Value("${config.root.folder}")
  private File outputFolder;

  @Autowired
  protected StatisticRepository(final CandlestickService candlestickService) {
    this.candlestickService = candlestickService;
  }

  @PostConstruct
  private void init() {
    final List<LocalTime> localTimeList = IntStream.range(0, 1440).parallel().mapToObj(minut -> LocalTime.of(0, 0).plusMinutes(minut)).map(
            localTime -> this.getCandlestickService()
                .getCandleDateTime(LocalDateTime.of(LocalDate.now(), localTime), TimeFrame.valueOf(this.getCandlestickService().getTimeFrame())))
        .map(LocalDateTime::toLocalTime).map(localTime -> localTime.withSecond(0)).map(localTime -> localTime.withNano(0)).toList();
    Arrays.stream(DayOfWeek.values()).filter(dayOfWeek -> !dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY)).forEachOrdered(dayOfWeek -> this.getDataBase().put(dayOfWeek,
        localTimeList.parallelStream().map(localTime -> new SimpleEntry<>(localTime, Pair.of(new AtomicInteger(0), new AtomicInteger(0))))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> a, TreeMap::new))));
    this.printDebugHeader();
  }

  @SneakyThrows
  private void printDebugHeader() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
      csvPrinter.printRecord("DAY", "TIME", "WIN", "LOSE");
    }
  }

  public void addResultWin(final @NotNull LocalDateTime localDateTime) {
    final LocalDateTime candleDateTime = this.getCandlestickService().getCandleDateTime(localDateTime, TimeFrame.valueOf(this.getCandlestickService().getTimeFrame()));
    this.getDataBase().get(candleDateTime.getDayOfWeek()).get(candleDateTime.toLocalTime()).getKey().incrementAndGet();
  }

  public void addResultLose(final @NotNull LocalDateTime localDateTime) {
    final LocalDateTime candleDateTime = this.getCandlestickService().getCandleDateTime(localDateTime, TimeFrame.valueOf(this.getCandlestickService().getTimeFrame()));
    this.getDataBase().get(candleDateTime.getDayOfWeek()).get(candleDateTime.toLocalTime()).getValue().incrementAndGet();
  }

  @PreDestroy
  private void preDestroy() {
    this.updateDebugFile();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), OUTPUT_FILE_NAME);
  }

  @SneakyThrows
  private void updateDebugFile() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
      this.getDataBase().forEach((dayOfWeek, localTimePairTreeMap) -> localTimePairTreeMap.forEach((localTime, atomicIntegerAtomicIntegerPair) -> {
        try {
          csvPrinter.printRecord(dayOfWeek, localTime.format(DateTimeFormatter.ofPattern("HH:mm")), atomicIntegerAtomicIntegerPair.getKey().get(),
              atomicIntegerAtomicIntegerPair.getValue().get());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }));
    }
  }

}
