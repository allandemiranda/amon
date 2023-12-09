package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.GeneratorProcessor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class SpringConfig {

  private final GeneratorProcessor generatorProcessor;
  @Value("${config.mock.ticket.input}")
  private File inputFile;

  @Value("${config.statistic.fileName}")
  private String fileName;
  @Value("${chart.timeframe:M15}")
  private String timeFrame;
  @Value("${order.open.onlyStrong:false}")
  private boolean isOpenOnlyStrong;
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

  @Autowired
  public SpringConfig(GeneratorProcessor generatorProcessor) {
    this.generatorProcessor = generatorProcessor;
  }

  @Bean
  void processor() {
    // MOCKED
    try (final FileReader fileReader = new FileReader(this.getInputFile()); final CSVParser csvParser = CSVFormat.TDF.builder().build().parse(fileReader)) {
      StreamSupport.stream(csvParser.spliterator(), false).skip(1).forEachOrdered(csvRecord -> {
        String date = csvRecord.get(0);
        String time = csvRecord.get(1);
        String dataTime = date.replace(".", "-").concat("T").concat(time);
        LocalDateTime localDateTime = LocalDateTime.parse(dataTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Double bid = csvRecord.get(2).isEmpty() ? null : Double.parseDouble(csvRecord.get(2));
        Double ask = csvRecord.get(3).isEmpty() ? null : Double.parseDouble(csvRecord.get(3));
        this.getGeneratorProcessor().webSocket(localDateTime, bid, ask);
      });
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
