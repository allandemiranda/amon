package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.enums.TimeFrame;
import br.eti.allandemiranda.forex.services.CandlestickService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StatisticRepository {

  //! This is a temporary class to generate temporary statistic values for performance of results
  //! This class needs to be removed at the end of this project

  private static final String TIME_START = "00:00:01";
  private static final String ERROR = "ERROR!";
  private static final String STR = " - ";
  private static final CSVFormat CSV_FORMAT = CSVFormat.TDF.builder().build();
  private final CandlestickService candlestickService;
  private final TreeMap<DayOfWeek, TreeMap<LocalTime, Pair<AtomicInteger, AtomicInteger>>> dataBase = new TreeMap<>();
  @Value("${config.statistic.fileName}")
  private String fileName;
  @Value("${chart.timeframe:M15}")
  private String timeFrame;
  @Value("${order.open.monday.start:00:00:00}")
  private String mondayStart;
  @Value("${order.open.monday.end:23:59:59}")
  private String mondayEnd;
  @Value("${order.open.tuesday.start:00:00:00}")
  private String tuesdayStart;
  @Value("${order.open.tuesday.end:23:59:59}")
  private String tuesdayEnd;
  @Value("${order.open.wednesday.start:00:00:00}")
  private String wednesdayStart;
  @Value("${order.open.wednesday.end:23:59:59}")
  private String wednesdayEnd;
  @Value("${order.open.thursday.start:00:00:00}")
  private String thursdayStart;
  @Value("${order.open.thursday.end:23:59:59}")
  private String thursdayEnd;
  @Value("${order.open.friday.start:00:00:00}")
  private String fridayStart;
  @Value("${order.open.friday.end:23:59:59}")
  private String fridayEnd;
  @Value("${order.open.spread.max:12}")
  private int maxSpread;
  @Value("${order.safe.take-profit:150}")
  private int takeProfit;
  @Value("${order.safe.stop-loss:100}")
  private int stopLoss;
  @Value("${order.open.trading.min:-1}")
  private int minTradingDiff;
  @Value("${order.open.onlyStrong:false}")
  private boolean isOpenOnlyStrong;
  private BigDecimal highBalance = BigDecimal.ZERO;
  private BigDecimal lowBalance = BigDecimal.ZERO;
  private BigDecimal currentBalance = BigDecimal.ZERO;

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
    Arrays.stream(DayOfWeek.values()).filter(dayOfWeek -> !dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY)).forEachOrdered(
        dayOfWeek -> this.getDataBase().put(dayOfWeek,
            localTimeList.parallelStream().map(localTime -> new SimpleEntry<>(localTime, Pair.of(new AtomicInteger(0), new AtomicInteger(0))))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> a, TreeMap::new))));
  }

  public void setBalance(final @NotNull BigDecimal balance) {
    if (balance.compareTo(this.getHighBalance()) > 0) {
      this.setHighBalance(balance);
    } else if (balance.compareTo(this.getLowBalance()) < 0) {
      this.setLowBalance(balance);
    }
    this.setCurrentBalance(balance);
  }

//  @SneakyThrows
//  private void printDebugHeader() {
//    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
//      csvPrinter.printRecord("*TIME FRAME", "*SLOT OPEN DAY", "*SLOT OPEN TIME", "*TP", "*SL", "*MAX SPREAD", "*MIN TRADING", "*ONLY STRONG", "WIN %", "WIN", "LOSE",
//          "TOTAL POSITION", "CONSISTENCE %", "NUMBER OF BAR", "LOW POINT", "HIGH POINT", "FINAL BALANCE");
//    }
//  }

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
//    this.printDebugHeader();
    this.updateDebugFile();
  }

  private @NotNull File getOutputFile() {
    return new File(this.getOutputFolder(), fileName.concat(".csv"));
  }

  private @NotNull String getNumber(final @NotNull BigDecimal value) {
    return new DecimalFormat("#0.0#").format(value.doubleValue()).replace(".", ",");
  }

  @SneakyThrows
  private void updateDebugFile() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile()); final CSVPrinter csvPrinter = CSV_FORMAT.print(fileWriter)) {
      final int win = this.getDataBase().entrySet().stream().flatMapToInt(entry -> entry.getValue().values().stream().mapToInt(integerPair -> integerPair.getKey().get()))
          .sum();
      final int lose = this.getDataBase().entrySet().stream()
          .flatMapToInt(entry -> entry.getValue().values().stream().mapToInt(integerPair -> integerPair.getValue().get())).sum();
      final int total = win + lose;

      final BigDecimal winPorc =
          win == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(win).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

      final long numberBar = this.getCandlestickService().getNumberBar();

      final BigDecimal consistence = total == 0 || numberBar == 0L ? BigDecimal.ZERO
          : BigDecimal.valueOf(total).divide(BigDecimal.valueOf(numberBar), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

      csvPrinter.printRecord(this.getTimeFrame(), this.getSlotOpen().getKey(), this.getSlotOpen().getValue(), this.getTakeProfit(), this.getStopLoss(),
          this.getMaxSpread(), this.getMinTradingDiff(), this.isOpenOnlyStrong(), this.getNumber(winPorc), win, lose, total, this.getNumber(consistence), numberBar,
          this.getNumber(this.getLowBalance()), this.getNumber(this.getHighBalance()), this.getNumber(this.getCurrentBalance()));

      log.info("=BALANCE={}=BALANCE=", this.getCurrentBalance());
      log.info("=WIN={}=WIN=", win);
      log.info("=LOSE={}=LOSE=", lose);
    }
  }

  private @NotNull Pair<String, String> getSlotOpen() {
    if (!this.getMondayStart().equals(TIME_START)) {
      return Pair.of(DayOfWeek.MONDAY.toString(), (this.getMondayStart()).concat(STR).concat(this.getMondayEnd()));
    } else if (!this.getTuesdayStart().equals(TIME_START)) {
      return Pair.of(DayOfWeek.TUESDAY.toString(), (this.getTuesdayStart()).concat(STR).concat(this.getTuesdayEnd()));
    } else if (!this.getWednesdayStart().equals(TIME_START)) {
      return Pair.of(DayOfWeek.WEDNESDAY.toString(), (this.getWednesdayStart()).concat(STR).concat(this.getWednesdayEnd()));
    } else if (!this.getThursdayStart().equals(TIME_START)) {
      return Pair.of(DayOfWeek.THURSDAY.toString(), (this.getThursdayStart()).concat(STR).concat(this.getThursdayEnd()));
    } else if (!this.getFridayStart().equals(TIME_START)) {
      return Pair.of(DayOfWeek.FRIDAY.toString(), (this.getFridayStart()).concat(STR).concat(this.getFridayEnd()));
    }
    return Pair.of(ERROR, ERROR);
  }

}
