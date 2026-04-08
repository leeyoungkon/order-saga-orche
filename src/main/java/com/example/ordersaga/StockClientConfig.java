package com.example.ordersaga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StockClientConfig {

    @Bean
    public RestClient stockRestClient(@Value("${stock.saga.url}") String stockSagaUrl) {
        return RestClient.builder()
                .baseUrl(stockSagaUrl)
                .build();
    }
}