package com.ticket.booking_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Cố gắng lấy khóa (lock) phân tán cho một key cụ thể.
     * @param lockKey Key để khóa (ví dụ: lock:seat:UUID)
     * @param value Giá trị định danh cho người giữ khóa (ví dụ: bookingId hoặc requestId)
     * @param expireTimeMs Thời gian khóa tự động hết hạn (mili-giây) đề phòng deadlock
     * @return true nếu lấy khóa thành công, false nếu khóa đang bị giữ bởi request khác
     */
    public boolean acquireLock(String lockKey, String value, long expireTimeMs) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                value,
                Duration.ofMillis(expireTimeMs)
        );
        return success != null && success;
    }

    /**
     * Giải phóng khóa (unlock).
     * Chỉ giải phóng nếu giá trị hiện tại trùng khớp với giá trị định danh lúc khóa (tránh việc giải phóng nhầm khóa của request khác khi bị timeout).
     */
    public void releaseLock(String lockKey, String value) {
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (value.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}
