package buy01.gateway_service.service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserBlacklistService {

    private final ReactiveStringRedisTemplate reactiveRedisTemplate;

    public UserBlacklistService(ReactiveStringRedisTemplate reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Boolean> isBlacklisted(String token) {
        // Correct asynchronous reactive check
        return reactiveRedisTemplate.hasKey(token);
    }
}