package br.eti.allandemiranda.forex.controllers;

import br.eti.allandemiranda.forex.services.CandlestickService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class Test {

  private final CandlestickService candlestickService;

  @Autowired
  public Test(CandlestickService candlestickService) {
    this.candlestickService = candlestickService;
  }

  public void start() {
    Arrays.stream(candlestickService.getCloseValues()).forEachOrdered(System.out::println);

  }
}
