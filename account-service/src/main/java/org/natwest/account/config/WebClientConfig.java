package org.natwest.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    public WebClient transactionWebClient(WebClient.Builder builder){
        return builder.baseUrl("http://localhost:8082").build();
    }
}
