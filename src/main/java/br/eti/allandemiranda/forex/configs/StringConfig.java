package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.GeneratorProcessor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StringConfig {

  private final GeneratorProcessor generatorProcessor;
  @Value("${config.mock.ticket.input}")
  private File inputFile;

  @Autowired
  public StringConfig(GeneratorProcessor generatorProcessor) {
    this.generatorProcessor = generatorProcessor;
  }

  @Bean
  void processor() {
    // MOCKED
    try (final FileReader fileReader = new FileReader(inputFile); final CSVParser csvParser = CSVFormat.TDF.builder().build()
        .parse(fileReader)) {
      StreamSupport.stream(csvParser.spliterator(), false)
          .skip(1)
          .limit(4000000).forEach(csvRecord -> {
            String date = csvRecord.get(0);
            String time = csvRecord.get(1);
            String dataTime = date.replace(".", "-").concat("T").concat(time);
            LocalDateTime localDateTime = LocalDateTime.parse(dataTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Double bid = csvRecord.get(2).isEmpty() ? null : Double.parseDouble(csvRecord.get(2));
            Double ask = csvRecord.get(3).isEmpty() ? null : Double.parseDouble(csvRecord.get(3));
            generatorProcessor.webSocket(localDateTime, bid, ask);
          });
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
