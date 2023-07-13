package br.eti.allandemiranda.forex.repositories;

import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CandlestickRepository {

    private Collection<CandlestickEntity> dataBase = new ArrayList<>();

    @Value("classpath:files/EURUSD_202301020007_202307072357.csv")
    private Resource resource;

    @SneakyThrows
    public @NotNull File getInputFile() {
        return resource.getFile();
    }

    @NotNull
    public Collection<CandlestickEntity> getDataBase() {
        return this.dataBase;
    }

    public void initDataBase(@NotNull Stream<Ticket> ticketStream) {
        AtomicLong idx = new AtomicLong(1L);
        dataBase = ticketStream.collect(Collectors.groupingBy(o -> o.dataTime().getYear(), Collectors.groupingBy(o -> o.dataTime().getMonth(), Collectors.groupingBy(o -> o.dataTime().getDayOfMonth(), Collectors.groupingBy(o -> o.dataTime().getHour(), Collectors.groupingBy(o -> o.dataTime().getMinute() / 15, Collectors.toCollection(ArrayList::new))))))).entrySet().stream().flatMap(year -> year.getValue().entrySet().stream().flatMap(month -> month.getValue().entrySet().stream().flatMap(day -> day.getValue().entrySet().stream().flatMap(hour -> hour.getValue().entrySet().stream().map(div -> {
            CandlestickEntity candlestickEntity = new CandlestickEntity();
            long firstIdx = div.getValue().stream().mapToLong(Ticket::idx).min().orElse(0L);
            div.getValue().stream().sorted(Comparator.comparingLong(Ticket::idx)).forEachOrdered(ticket -> {
                if (ticket.idx() == firstIdx) {
                    candlestickEntity.setIdx(ticket.idx());
                    candlestickEntity.setOpen(ticket.bid());
                    candlestickEntity.setLow(ticket.bid());
                    candlestickEntity.setHigh(ticket.bid());

                    LocalTime localTime = LocalTime.of(ticket.dataTime().getHour(), div.getKey() * 15, 0, 0);
                    LocalDateTime localDateTime = LocalDateTime.of(ticket.dataTime().toLocalDate(), localTime);
                    candlestickEntity.setOpenTime(localDateTime);
                } else {
                    if (ticket.bid() < candlestickEntity.getLow()) {
                        candlestickEntity.setLow(ticket.bid());
                    }
                    if (ticket.bid() > candlestickEntity.getHigh()) {
                        candlestickEntity.setHigh(ticket.bid());
                    }
                }
                candlestickEntity.setClose(ticket.bid());
            });
            if(candlestickEntity.getIdx() == 0L){
                // TODO
            }
            return candlestickEntity;
        }))))).sorted(Comparator.comparingLong(CandlestickEntity::getIdx)).map(candlestickEntity -> {
            candlestickEntity.setIdx(idx.getAndIncrement());
            return candlestickEntity;
        }).collect(Collectors.toCollection(ArrayList::new));
    }
}
