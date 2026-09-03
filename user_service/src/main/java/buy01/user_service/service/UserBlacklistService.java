package buy01.user_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void blacklistUser(String userId) {
        redisTemplate.opsForValue().set(userId, "BLACKLISTED");
    }
}