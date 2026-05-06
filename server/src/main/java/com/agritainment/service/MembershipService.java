package com.agritainment.service;

import com.agritainment.common.AppException;
import com.agritainment.entity.Coupon;
import com.agritainment.entity.MembershipConfig;
import com.agritainment.entity.User;
import com.agritainment.mapper.CouponMapper;
import com.agritainment.mapper.MembershipConfigMapper;
import com.agritainment.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserMapper userMapper;
    private final MembershipConfigMapper membershipConfigMapper;
    private final CouponMapper couponMapper;
    private final UserService userService;

    public Map<String, Object> getStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new AppException(40403, "用户不存在");
        Map<String, Object> status = new HashMap<>();
        status.put("isMember", user.getIsMember());
        status.put("expireAt", user.getMemberExpireAt());
        MembershipConfig config = getActiveConfig();
        if (config != null) {
            status.put("annualPrice", config.getAnnualPrice());
            status.put("discountRate", config.getDiscountRate());
        }
        return status;
    }

    @Transactional
    public Map<String, Object> purchase(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new AppException(40403, "用户不存在");

        MembershipConfig config = getActiveConfig();
        if (config == null) throw new AppException(40301, "会员配置不存在");

        LocalDate expireAt = user.getIsMember() != null && user.getIsMember() && user.getMemberExpireAt() != null
                ? user.getMemberExpireAt().plusYears(1)
                : LocalDate.now().plusYears(1);

        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getIsMember, true)
                .set(User::getMemberExpireAt, expireAt));

        Map<String, Object> result = new HashMap<>();
        result.put("expireAt", expireAt);
        result.put("price", config.getAnnualPrice());

        if (config.getGiftProductIds() != null) {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<?> productIds;
            try {
                productIds = mapper.readValue(config.getGiftProductIds(), java.util.List.class);
            } catch (Exception e) {
                throw new AppException(40302, "会员赠送产品配置异常");
            }
            for (Object pid : productIds) {
                Long productId = Long.valueOf(pid.toString());
                String code = userService.generateCouponCode();
                Coupon coupon = new Coupon();
                coupon.setCode(code);
                coupon.setUserId(userId);
                coupon.setProductId(productId);
                coupon.setSource("membership");
                coupon.setStatus("available");
                coupon.setQrCodeData("MEMBER_" + code);
                couponMapper.insert(coupon);
            }
        }

        return result;
    }

    public MembershipConfig getConfig() {
        return getActiveConfig();
    }

    @Transactional
    public MembershipConfig updateConfig(Double annualPrice, Double discountRate, String giftProductIds) {
        MembershipConfig config = getActiveConfig();
        if (config == null) {
            config = new MembershipConfig();
            config.setAnnualPrice(annualPrice);
            config.setDiscountRate(discountRate);
            config.setGiftProductIds(giftProductIds);
            membershipConfigMapper.insert(config);
        } else {
            LambdaUpdateWrapper<MembershipConfig> wrapper = new LambdaUpdateWrapper<MembershipConfig>()
                    .eq(MembershipConfig::getId, config.getId());
            if (annualPrice != null) wrapper.set(MembershipConfig::getAnnualPrice, annualPrice);
            if (discountRate != null) wrapper.set(MembershipConfig::getDiscountRate, discountRate);
            if (giftProductIds != null) wrapper.set(MembershipConfig::getGiftProductIds, giftProductIds);
            membershipConfigMapper.update(null, wrapper);
            config = membershipConfigMapper.selectById(config.getId());
        }
        return config;
    }

    @Transactional
    public void grant(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new AppException(40403, "用户不存在");

        LocalDate expireAt = user.getIsMember() != null && user.getIsMember() && user.getMemberExpireAt() != null
                ? user.getMemberExpireAt().plusYears(1)
                : LocalDate.now().plusYears(1);

        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getIsMember, true)
                .set(User::getMemberExpireAt, expireAt));
    }

    private MembershipConfig getActiveConfig() {
        return membershipConfigMapper.selectOne(new LambdaQueryWrapper<MembershipConfig>().last("LIMIT 1"));
    }
}
