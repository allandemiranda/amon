package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.exceptions.DataRepositoryException;
import br.eti.allandemiranda.forex.exceptions.WriteFileException;
import br.eti.allandemiranda.forex.headers.TicketHeaders;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

  private static final String TARGET = ".";
  private static final String REPLACEMENT = ",";

  @Value("${ticket.pip}")
  private Double pip;
  @Value("${ticket.repository.output}")
  private File outputFile;

  private LocalDateTime localDateTime;
  private Double bid;
  private Double ask;

  @PostConstruct
  public void init() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord((Object[]) TicketHeaders.values());
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  public void updateOutputFile() {
    try (final FileWriter fileWriter = new FileWriter(this.getOutputFile(), true); final CSVPrinter csvPrinter = CSVFormat.TDF.builder().build().print(fileWriter)) {
      csvPrinter.printRecord(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME), String.valueOf(bid).replace(TARGET, REPLACEMENT),
          String.valueOf(ask).replace(TARGET, REPLACEMENT));
    } catch (IOException e) {
      throw new WriteFileException(e);
    }
  }

  private File getOutputFile() {
    return this.outputFile;
  }

  private double getPip() {
    return this.pip;
  }

  public LocalDateTime getLocalDateTime() {
    return this.localDateTime;
  }

  private void setLocalDateTime(final @NotNull LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  public double getBid() {
    return this.getTicket().bid();
  }

  private void setBid(final Double bid) {
    this.bid = bid;
  }

  private double getAsk() {
    return this.getTicket().ask();
  }

  public void setAsk(final Double ask) {
    this.ask = ask;
  }

  public double getSpread() {
    return (getAsk() - getBid()) / getPip();
  }

  public void add(final @NotNull Ticket ticket) {
    if (ticket.dateTime().isAfter(this.getLocalDateTime())) {
      this.setLocalDateTime(ticket.dateTime());
      if (Objects.nonNull(ticket.bid())) {
        this.setBid(ticket.bid());
      }
      if (Objects.nonNull(ticket.ask())) {
        this.setAsk(ticket.ask());
      }
    } else {
      throw new DataRepositoryException(String.format("Ticket with data %s before current %s", this.getLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME),
          ticket.dateTime().format(DateTimeFormatter.ISO_DATE_TIME)));
    }
  }

  public @NotNull Ticket getTicket() {
    if (Objects.isNull(this.bid) || Objects.isNull(this.ask)) {
      throw new DataRepositoryException("Can't provide a incomplete ticket");
    } else {
      return new Ticket(this.getLocalDateTime(), this.getBid(), this.getAsk());
    }
  }
}
