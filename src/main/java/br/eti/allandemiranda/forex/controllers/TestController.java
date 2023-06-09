package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.services.CandlestickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TestController {

    final CandlestickService candlestickService;

    @Autowired
    public TestController(CandlestickService candlestickService) {
        this.candlestickService = candlestickService;
    }

    public void getResult() {
        candlestickService.getAll().stream().limit(10).forEach(System.out::println);
    }

}
