package com.deliverytech.delivery.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.web.client.RestTemplateBuilder; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate; 
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Configurações específicas do ModelMapper
        mapper.getConfiguration()
              .setMatchingStrategy(MatchingStrategies.STRICT)
              .setFieldMatchingEnabled(true)
              .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        return mapper;
    }

   
    /**
     * Cria um Bean gerenciado pelo Spring para o RestTemplate.
     * Isso permite que ele seja injetado em outros componentes
     * e configurado centralmente.
     * * @param builder O Spring injeta o RestTemplateBuilder automaticamente.
     * @return Uma instância de RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Usamos o RestTemplateBuilder para construir o RestTemplate
        // Isso garante que timeouts, interceptors (como o de Tracing)
        // sejam aplicados corretamente.
        return builder.build();
    }
}