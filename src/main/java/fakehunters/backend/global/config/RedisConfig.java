package fakehunters.backend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer serializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(serializer)
                .key(serializer)
                .value(serializer)
                .hashKey(serializer)
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    @Primary
    public CommandLineRunner testRedis(ReactiveRedisTemplate<String, String> redisTemplate) {
        return args -> {
            log.info("=== Redis 연결 테스트 시작 ===");

            // 쓰기 테스트
            redisTemplate.opsForValue()
                    .set("test_key", "test_value")
                    .doOnSuccess(result -> log.info("Redis 쓰기 성공: {}", result))
                    .doOnError(e -> log.error("Redis 쓰기 실패", e))
                    .subscribe();

            // 읽기 테스트
            Thread.sleep(100); // 쓰기가 완료될 때까지 대기

            redisTemplate.opsForValue()
                    .get("test_key")
                    .doOnNext(value -> log.info("Redis 읽기 성공: {}", value))
                    .doOnError(e -> log.error("Redis 읽기 실패", e))
                    .subscribe();

            log.info("=== Redis 연결 테스트 완료 ===");
        };
    }
}