package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StringConfig {
//TODO
  private final Test test;

  @Autowired
  public StringConfig(Test test) {
    this.test = test;
  }

  @Bean
  public void getTest() {
    test.start();
  }
}
