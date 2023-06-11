package br.eti.allandemiranda.forex.configs;

import br.eti.allandemiranda.forex.controllers.CandlestickController;
import br.eti.allandemiranda.forex.models.CandlestickModel;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.batch.api.chunk.ItemReader;

@Configuration
public class BatchConfig {

    private final CandlestickController candlestickController;

    @Autowired
    public BatchConfig(CandlestickController candlestickController) {
        this.candlestickController = candlestickController;
    }

//    @Bean
//    public FlatFileItemReaderBuilder<CandlestickModel> reader() {
//        return new FlatFileItemReaderBuilder<Person>()
//                .name("personItemReader")
//                .resource(new ClassPathResource("sample-data.csv"))
//                .delimited()
//                .names(new String[]{"firstName", "lastName"})
//                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
//                    setTargetType(Person.class);
//                }})
//                .build();
//    }

    @Bean
    public void run() {
        candlestickController.getResult().forEach(candlestickDto -> System.out.println(candlestickDto));
    }
}
