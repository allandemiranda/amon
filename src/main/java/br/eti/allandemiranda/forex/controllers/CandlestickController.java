package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.dtos.CandlestickDto;
import br.eti.allandemiranda.forex.services.CandlestickService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CandlestickController {

    private final CandlestickService candlestickService;

    @Autowired
    public CandlestickController(CandlestickService candlestickService) {
        this.candlestickService = candlestickService;
    }

    public List<CandlestickDto> getResult() {
        return candlestickService.getAll().stream().limit(10).map(candlestickModel ->  new ModelMapper().map(candlestickModel, CandlestickDto.class)).toList();
    }
}
