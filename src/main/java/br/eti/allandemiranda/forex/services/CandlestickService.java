package br.eti.allandemiranda.forex.services;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.dtos.Ticket;
import br.eti.allandemiranda.forex.entities.CandlestickEntity;
import br.eti.allandemiranda.forex.repositories.CandlestickRepository;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CandlestickService {

    private final CandlestickRepository repository;

    @Autowired
    @Inject
    protected CandlestickService(CandlestickRepository repository) {
        this.repository = repository;
    }

    @NotNull
    public Stream<Candlestick> selectAll(Stream<Ticket> tickets) {
        repository.initDataBase(tickets);
        return repository.getDataBase().stream().map(getCandlestickEntityCandlestickFunction());
    }

    @NotNull
    private Function<CandlestickEntity, Candlestick> getCandlestickEntityCandlestickFunction() {
        return candlestickEntity -> new Candlestick(candlestickEntity.getIdx(), candlestickEntity.getOpenTime(), candlestickEntity.getOpen(), candlestickEntity.getHigh(), candlestickEntity.getLow(), candlestickEntity.getClose());
    }
}
