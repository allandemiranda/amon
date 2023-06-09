package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    private final TestController testController;

    @Autowired
    public BatchConfig(TestController testController) {
        this.testController = testController;
    }

    @Bean
    public void run(){
        testController.getResult();
    }
}
