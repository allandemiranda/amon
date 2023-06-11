package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.dtos.Candlestick;
import br.eti.allandemiranda.forex.services.CandlestickService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GraphicController {

    private final CandlestickService candlestickService;

    @Autowired
    public GraphicController(CandlestickService candlestickService) {
        this.candlestickService = candlestickService;
    }

    public List<Candlestick> getCandlesticks() {
        return candlestickService.getAll().stream().map(candlestickModel ->  new ModelMapper().map(candlestickModel, Candlestick.class)).toList();
    }

    public List<String> getSignal(){
//        return CommodityChannelIndex
    }


}
