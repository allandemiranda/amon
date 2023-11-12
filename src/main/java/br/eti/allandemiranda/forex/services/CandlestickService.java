package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.exceptions.ServiceException;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import br.eti.allandemiranda.forex.utils.TimeFrame;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter(AccessLevel.PRIVATE)
public class CandlestickService {

  private static final int MINUTE_ZERO = 0;
  private static final int MINUTE_FIFTEEN = 15;
  private static final int MINUTE_THIRTY = 30;
  private static final int MINUTE_FORTY_FIVE = 45;
  private static final int TIME_FRAME_ONE = 1;
  private static final int TIME_FRAME_FIVE = 5;
  private static final int TIME_FRAME_TWO = 2;
  private static final int ONE_HOUR_MIN = 60;
  private static final int ONE_DAY_HOUR = 24;
  private static final int SKIP_CURRENT_CANDLESTICK = 1;

  private final CandlestickRepository repository;

  @Autowired
  protected CandlestickService(final CandlestickRepository repository) {
    this.repository = repository;
  }

  /**
   * Get the candlestick DataTime to which Ticket DataTime belongs
   *
   * @param dataTime  The Ticket DataTime
   * @param timeFrame The time frame request
   * @return The DataTime to the candlestick
   */
  private static @NotNull LocalDateTime getCandleDateTime(final @NotNull LocalDateTime dataTime, final @NotNull TimeFrame timeFrame) {
    return switch (timeFrame) {
      case M1 -> getDateTimeLowM(dataTime, TIME_FRAME_ONE);
      case M5 -> getDateTimeLowM(dataTime, TIME_FRAME_FIVE);
      case M15 -> getDateTimeToM15(dataTime);
      case M30 -> getDateTimeToM30(dataTime);
      case H1 -> getDateTimeLowH(dataTime, TIME_FRAME_ONE);
      case H2 -> getDateTimeLowH(dataTime, TIME_FRAME_TWO);
      case D1 -> getDateTimeOneDay(dataTime);
    };
  }

  /**
   * Get DateTime for a candlestick for time frame D1
   *
   * @param ticketDateTime The Ticket DataTime request
   * @return The DataTime of candlestick
   */
  private static @NotNull LocalDateTime getDateTimeOneDay(final @NotNull LocalDateTime ticketDateTime) {
    return ticketDateTime.toLocalDate().atStartOfDay();
  }

  /**
   * Get DateTime for a candlestick for time frame with low minute value
   *
   * @param ticketDateTime The Ticket DataTime request
   * @param timeFrameMin   The low minute time frame request
   * @return The DataTime of candlestick
   */
  private static @NotNull LocalDateTime getDateTimeLowM(final @NotNull LocalDateTime ticketDateTime, final int timeFrameMin) {
    final int[] minArray = IntStream.rangeClosed(0, ONE_HOUR_MIN / timeFrameMin).map(operand -> timeFrameMin * operand).toArray();
    final int index = IntStream.range(TIME_FRAME_ONE, minArray.length).filter(i -> ticketDateTime.getMinute() < minArray[i]).findFirst()
        .orElseThrow(IllegalStateException::new);
    return LocalDateTime.of(ticketDateTime.toLocalDate(), LocalTime.of(ticketDateTime.getHour(), minArray[index - TIME_FRAME_ONE]));
  }

  /**
   * Get DateTime for a candlestick for time frame with low hours value
   *
   * @param ticketDateTime The Ticket DataTime request
   * @param timeFrameHour  The low hours time frame request
   * @return The DataTime of candlestick
   */
  private static @NotNull LocalDateTime getDateTimeLowH(final @NotNull LocalDateTime ticketDateTime, final int timeFrameHour) {
    final int[] hourArray = IntStream.rangeClosed(0, ONE_DAY_HOUR / timeFrameHour).map(operand -> operand * timeFrameHour).toArray();
    final int index = IntStream.range(TIME_FRAME_ONE, hourArray.length).filter(i -> ticketDateTime.getHour() < hourArray[i]).findFirst()
        .orElseThrow(IllegalStateException::new);
    return LocalDateTime.of(ticketDateTime.toLocalDate(), LocalTime.of(hourArray[index - TIME_FRAME_ONE], 0));
  }

  /**
   * Get DateTime for a candlestick for time frame M15
   *
   * @param ticketDateTime The Ticket DataTime request
   * @return The DataTime of candlestick
   */
  private static @NotNull LocalDateTime getDateTimeToM15(final @NotNull LocalDateTime ticketDateTime) {
    final LocalDate localDate = ticketDateTime.toLocalDate();
    final int hour = ticketDateTime.getHour();
    final int minute = ticketDateTime.getMinute();
    if (minute < MINUTE_FIFTEEN) {
      return LocalDateTime.of(localDate, LocalTime.of(hour, MINUTE_ZERO));
    } else if (minute < MINUTE_THIRTY) {
      return LocalDateTime.of(localDate, LocalTime.of(hour, MINUTE_FIFTEEN));
    } else if (minute < MINUTE_FORTY_FIVE) {
      return LocalDateTime.of(localDate, LocalTime.of(hour, MINUTE_THIRTY));
    } else {
      return LocalDateTime.of(localDate, LocalTime.of(hour, MINUTE_FORTY_FIVE));
    }
  }

  /**
   * Get DateTime for a candlestick for time frame M30
   *
   * @param ticketDateTime The Ticket DataTime request
   * @return The DataTime of candlestick
   */
  private static @NotNull LocalDateTime getDateTimeToM30(final @NotNull LocalDateTime ticketDateTime) {
    final LocalDate localDate = ticketDateTime.toLocalDate();
    final int hour = ticketDateTime.getHour();
    if (ticketDateTime.getMinute() < MINUTE_THIRTY) {
      return LocalDateTime.of(localDate, LocalTime.of(hour, MINUTE_ZERO));
    } else {
      return LocalDateTime.of(localDate, LocalTime.of(hour, MINUTE_THIRTY));
    }
  }

  /**
   * To add a Ticket data on the chart of candlesticks
   *
   * @param ticket    The ticket to be add
   * @param timeFrame The time frame of chart candlesticks
   */
  public void addTicket(final @NotNull Ticket ticket, final @NotNull TimeFrame timeFrame) {
    final BigDecimal price = ticket.bid();
    final LocalDateTime candleDateTime = getCandleDateTime(ticket.dateTime(), timeFrame);
    this.getRepository().add(candleDateTime, price);
  }

  /**
   * Get the list of candlesticks close
   *
   * @param period The period of necessary candlesticks to be return (strategy to save the memory process)
   * @return The Stream of candlesticks close (not include the current one open) from newer to older
   */
  public Stream<Candlestick> getCandlesticksClose(final int period) {
    if (this.getRepository().getMemorySize() >= period + SKIP_CURRENT_CANDLESTICK) {
      return this.getRepository().get(period + SKIP_CURRENT_CANDLESTICK).skip(SKIP_CURRENT_CANDLESTICK);
    } else {
      throw new ServiceException("Can't get a Candlesticks period more high that the memory");
    }
  }

  /**
   * Get the list of candlesticks (the chart)
   *
   * @return The Stream of candlesticks
   */
  public Stream<Candlestick> getChart() {
    return this.getRepository().get();
  }

  /**
   * Check if the service of Candlesticks is ready to process data
   *
   * @return If the service of Candlesticks is ready to process data
   */
  public boolean isReady() {
    return this.getRepository().isReady();
  }

  /**
   * Get current open candlestick (last candlestick)
   *
   * @return The last candlestick
   */
  public @NotNull Candlestick getLastCandlestick() {
    return this.getRepository().getLastUpdate();
  }

  /**
   * Get the last candlestick close
   *
   * @return The last candlestick close
   */
  public @NotNull Candlestick getLastCloseCandlestick() {
    //! TODO: testar se isso aqui funciona, se pega mesmo o ultimo e se nos indicadores ele está calculando direito
    return this.getChart().skip(SKIP_CURRENT_CANDLESTICK).limit(1).findFirst()
        .orElseThrow(() -> new ServiceException("Can't last close Candlesticks because don't have memory to get"));
  }
}
