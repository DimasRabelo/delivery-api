package com.deliverytech.delivery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Configuração do Redis para a aplicação (Cache e Template).
 * <p>
 * Esta classe define os beans para o {@link CacheManager} e
 * {@link RedisTemplate}, incluindo uma lógica de fallback
 * para cache em memória ({@link ConcurrentMapCacheManager})
 * caso o servidor Redis não esteja disponível na inicialização.
 */
@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Cria o bean do CacheManager principal da aplicação.
     * <p>
     * Tenta se conectar ao Redis. Se for bem-sucedido, usa o
     * {@link RedisCacheManager} para cache distribuído.
     * <p>
     * Se a conexão com o Redis falhar (ex: em ambiente de dev local
     * sem Redis rodando), ele retorna um {@link ConcurrentMapCacheManager}
     * como fallback (cache em memória simples).
     * <p>
     * {@code @ConditionalOnMissingBean} permite que esta configuração
     * seja facilmente sobrescrita em testes (ex: com um 'NoOpCacheManager').
     *
     * @param redisConnectionFactory A factory de conexão injetada pelo Spring Boot.
     * @return Um CacheManager (Redis se disponível, ou em memória).
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class) // Permite que testes ou outros perfis sobrescrevam este bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        try {
            // 1. Tenta "pingar" o Redis para verificar a conexão
            redisConnectionFactory.getConnection().ping();
            logger.info("✅ Redis conectado com sucesso! Usando RedisCacheManager.");
            // 2. Se conectar, usa o CacheManager do Redis
            return RedisCacheManager.builder(redisConnectionFactory).build();
        
        } catch (RedisConnectionFailureException e) {
            // 3. Se falhar, avisa e usa o cache em memória como fallback
            logger.warn("⚠️ Redis não disponível. Usando cache em memória (ConcurrentMapCacheManager).");
            return new ConcurrentMapCacheManager();
        }
    }

    /**
     * Cria o bean do RedisTemplate para interações manuais com o Redis
     * (ex: Pub/Sub, operações de Hash, etc.), caso o RedisTemplate
     * padrão não atenda.
     * <p>
     * {@code @ConditionalOnMissingBean} permite que esta configuração
     * seja sobrescrita em testes.
     *
     * @param redisConnectionFactory A factory de conexão injetada pelo Spring Boot.
     * @return Um RedisTemplate configurado.
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class) // Permite que testes ou outros perfis sobrescream este bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // (Nota: Serializers (JSON/String) seriam configurados aqui se necessário)
        return template;
    }
}