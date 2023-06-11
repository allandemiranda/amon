package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.GraphicController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StringBatchConfig {

    private final GraphicController graphicController;

    @Autowired
    public StringBatchConfig(GraphicController graphicController) {
        this.graphicController = graphicController;
    }

    @Bean
    public void run() {
        graphicController.getResult().forEach(candlestickDto -> System.out.println(candlestickDto));
    }
}
