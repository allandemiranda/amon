package br.eti.allandemiranda.forex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ForexApplication {
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(ForexApplication.class, args)));
    }
}
