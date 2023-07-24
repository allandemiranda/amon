package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.chart.TicketsProcessor;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.headers.TicketHeaders;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StringConfig {

  @Value("${mock.ticket.input}")
  private File inputFile;

  private final TicketsProcessor ticketsProcessor;

  @Autowired
  public StringConfig(TicketsProcessor ticketsProcessor) {
    this.ticketsProcessor = ticketsProcessor;
  }

  @Bean
  void processor(){
    // MOCKED
    AtomicReference<Double> atomicBid = new AtomicReference<Double>(0D);
    AtomicReference<Double> atomicAsk = new AtomicReference<Double>(0D);
    try (final FileReader fileReader = new FileReader(inputFile); final CSVParser csvParser = CSVFormat.TDF.builder().setHeader(TicketHeaders.class).setSkipHeaderRecord(true).build().parse(fileReader)) {
      StreamSupport.stream(csvParser.spliterator(), false)
          .limit(5000)
          .map(csvRecord -> {
        String date = csvRecord.get(TicketHeaders.date);
        String time = csvRecord.get(TicketHeaders.time);
        String dataTime = date.replace(".", "-").concat("T").concat(time);
        LocalDateTime localDateTime =  LocalDateTime.parse(dataTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        double bid = csvRecord.get(TicketHeaders.bid).isEmpty() ? atomicBid.get() : Double.parseDouble(csvRecord.get(TicketHeaders.bid));
        atomicBid.set(bid);
        double ask = csvRecord.get(TicketHeaders.ask).isEmpty() ? atomicAsk.get() : Double.parseDouble(csvRecord.get(TicketHeaders.ask));
        atomicAsk.set(ask);
        return new Ticket(localDateTime, bid, ask);
      }).forEach(ticket -> {

        ticketsProcessor.socket(ticket);
      });
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
