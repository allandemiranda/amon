package br.eti.allandemiranda.forex.configurations;

import br.eti.allandemiranda.forex.controllers.TestController;
import br.eti.allandemiranda.forex.services.CandlestickService;
import br.eti.allandemiranda.forex.services.TicketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    private final TicketService ticketService;
    private final CandlestickService candlestickService;

    public Config(TicketService ticketService, CandlestickService candlestickService) {
        this.ticketService = ticketService;
        this.candlestickService = candlestickService;
    }

    @Bean(initMethod = "initJob")
    public TestController getTestController(){
        return new TestController(ticketService, candlestickService);
    }
}
