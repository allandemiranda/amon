package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.utils.TimeFrame;
import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandlestickService {

  private final CandlestickRepository candlestickRepository;
  private final TicketService ticketService;

  private LocalDateTime realDataTime = LocalDateTime.MIN;
  private LocalDateTime candlestickDataTime = LocalDateTime.MIN;

  @Value("${candlestick.timeframe}")
  private String timeFrame;

  @Autowired
  private CandlestickService(final CandlestickRepository candlestickRepository, final TicketService ticketService) {
    this.candlestickRepository = candlestickRepository;
    this.ticketService = ticketService;
  }

  private static @NotNull LocalDateTime getInputCandleDateTime(final @NotNull LocalDateTime ticketDateTime, final @NotNull TimeFrame timeFrame) {
    LocalDate localDate = ticketDateTime.toLocalDate();
    return switch (timeFrame) {
      //TODO implementar time frame para outros inputs
      case M1, M5, M30, H1 -> throw new IllegalStateException("Not implemented, only M15");
      case M15 -> getDateTimeToM15(ticketDateTime, localDate);
    };
  }

  private static @NotNull LocalDateTime getDateTimeToM15(final @NotNull LocalDateTime ticketDateTime, final @NotNull LocalDate localDate) {
    if (ticketDateTime.getMinute() < 15) {
      LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 0);
      return LocalDateTime.of(localDate, localTime);
    }
    if (ticketDateTime.getMinute() < 30) {
      LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 15);
      return LocalDateTime.of(localDate, localTime);
    }
    if (ticketDateTime.getMinute() < 45) {
      LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 30);
      return LocalDateTime.of(localDate, localTime);
    }
    LocalTime localTime = LocalTime.of(ticketDateTime.getHour(), 45);
    return LocalDateTime.of(localDate, localTime);
  }

  private static @NotNull Candlestick mergeCandleAndTicket(final @NotNull Candlestick candlestick, final @NotNull Ticket ticket) {
    if (candlestick.dateTime().equals(ticket.dateTime())) {
      if (ticket.bid() > candlestick.high()) {
        return candlestick.withClose(ticket.bid()).withHigh(ticket.bid());
      }
      if (ticket.bid() < candlestick.low()) {
        return candlestick.withClose(ticket.bid()).withLow(ticket.bid());
      }
      return candlestick.withClose(ticket.bid());
    } else {
      return new Candlestick(ticket.dateTime(), ticket.bid(), ticket.bid(), ticket.bid(), ticket.bid());
    }
  }

  private static @NotNull CandlestickEntity toEntity(@NotNull Candlestick model) {
    CandlestickEntity entity = new CandlestickEntity();
    entity.setDateTime(model.dateTime());
    entity.setLow(model.low());
    entity.setOpen(model.open());
    entity.setHigh(model.high());
    entity.setClose(model.close());
    return entity;
  }

  private static @NotNull Candlestick toModel(@NotNull CandlestickEntity entity) {
    return new Candlestick(entity.getDateTime(), entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose());
  }

  private @NotNull TimeFrame getTimeFrame() {
    return TimeFrame.valueOf(this.timeFrame);
  }

  public @NotNull LocalDateTime getRealDataTime() {
    return this.realDataTime;
  }

  private void setRealDataTime(final @NotNull LocalDateTime realDataTime) {
    this.realDataTime = realDataTime;
  }

  public @NotNull LocalDateTime getCandlestickDataTime() {
    return this.candlestickDataTime;
  }

  private void setCandlestickRepository(final @NotNull LocalDateTime candlestickDataTime) {
    this.candlestickDataTime = candlestickDataTime;
  }

  @Synchronized
  public void run() {
    this.setRealDataTime(this.ticketService.getLocalDateTime());
    this.setCandlestickRepository(getInputCandleDateTime(this.ticketService.getLocalDateTime(), this.getTimeFrame()));
    if (this.candlestickRepository.getDataBase().isEmpty()) {
      Candlestick model = new Candlestick(this.getCandlestickDataTime(), this.ticketService.getBid(), this.ticketService.getBid(), this.ticketService.getBid(),
          this.ticketService.getBid());
      this.candlestickRepository.addData(toEntity(model));
    } else {
      CandlestickEntity last = this.candlestickRepository.getLast();
      Candlestick merged = mergeCandleAndTicket(toModel(last), this.ticketService.getTicket().withDateTime(this.getCandlestickDataTime()));
      this.candlestickRepository.updateData(toEntity(merged));
    }
    this.candlestickRepository.saveRunTimeLine(this.candlestickRepository.getLast(), this.getRealDataTime());
  }

  public double getLastCloseValue() {
    return this.candlestickRepository.getLast().getClose();
  }

  public long getCurrentMemorySize() {
    return this.candlestickRepository.selectAll().size();
  }

  public double[] getCloseReversed(final int period) {
    return this.candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period)
        .mapToDouble(CandlestickEntity::getClose).toArray();
  }

  public double[] getHighReversed(final int period) {
    return this.candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period)
        .mapToDouble(CandlestickEntity::getHigh).toArray();
  }

  public double[] getLowReversed(final int period) {
    return this.candlestickRepository.selectAll().stream().sorted(Comparator.comparing(CandlestickEntity::getDateTime).reversed()).limit(period)
        .mapToDouble(CandlestickEntity::getLow).toArray();
  }
}
