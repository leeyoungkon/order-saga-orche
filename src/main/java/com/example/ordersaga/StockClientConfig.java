package com.example.ordersaga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class StockClientConfig {

    @Value("${stock.saga.url}")
    private String stockSagaUrl;

    @Bean
    public StockClient stockClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(stockSagaUrl)
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(StockClient.class);
    }
}
