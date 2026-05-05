package com.agritainment.service;

import com.agritainment.entity.User;
import com.agritainment.mapper.CouponMapper;
import com.agritainment.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final CouponMapper couponMapper;

    public boolean checkNoShow(Long userId, LocalDateTime reservationTime) {
        long hoursDiff = java.time.Duration.between(LocalDateTime.now(), reservationTime).toHours();
        if (hoursDiff < 6) {
            incrementNoShow(userId);
            return true;
        }
        return false;
    }

    public void incrementNoShow(Long userId) {
        User user = userMapper.selectById(userId);
        int newCount = (user.getNoShowCount() != null ? user.getNoShowCount() : 0) + 1;
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, userId)
                .set(User::getNoShowCount, newCount).set(User::getIsBlacklisted, newCount >= 3));
    }

    public String generateIdentityCode() {
        return generateUniqueCode(12);
    }

    public String generateCouponCode() {
        return generateUniqueCode(12);
    }

    private String generateUniqueCode(int length) {
        for (int i = 0; i < 3; i++) {
            String code = String.valueOf(100000000000L + ThreadLocalRandom.current().nextLong(900000000000L));
            Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getIdentityCode, code));
            Long couponCount = couponMapper.selectCount(new LambdaQueryWrapper<com.agritainment.entity.Coupon>().eq(com.agritainment.entity.Coupon::getCode, code));
            if (userCount == 0 && couponCount == 0) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique code after 3 attempts");
    }
}
