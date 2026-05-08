package com.agritainment.service;

import com.agritainment.annotation.BusinessLog;
import com.agritainment.common.AppException;
import com.agritainment.entity.Coupon;
import com.agritainment.entity.Product;
import com.agritainment.entity.User;
import com.agritainment.mapper.CouponMapper;
import com.agritainment.mapper.ProductMapper;
import com.agritainment.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final CouponMapper couponMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    public List<Product> getProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getIsAvailable, true).orderByAsc(Product::getId));
    }

    public Product getProduct(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new AppException(40101, "产品不存在");
        return p;
    }

    @Transactional
    @BusinessLog("购买产品")
    public Coupon purchase(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new AppException(40101, "产品不存在");
        if (!product.getIsAvailable()) throw new AppException(40102, "产品已下架");
        if (product.getRemainingQuota() != null && product.getRemainingQuota() != -1 && product.getRemainingQuota() <= 0)
            throw new AppException(40103, "产品已售罄");

        User user = userMapper.selectById(userId);
        BigDecimal price = Boolean.TRUE.equals(user.getIsMember()) && product.getMemberPrice() != null ? product.getMemberPrice() : product.getPrice();

        String code = userService.generateCouponCode();
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setProductId(productId);
        coupon.setUserId(userId);
        coupon.setSource("purchase");
        coupon.setStatus("available");
        coupon.setQrCodeData("COUPON_" + code);
        couponMapper.insert(coupon);

        if (product.getRemainingQuota() != null && product.getRemainingQuota() != -1) {
            if (product.getRemainingQuota() <= 0) throw new AppException(40103, "产品已售罄");
            product.setRemainingQuota(product.getRemainingQuota() - 1);
            int updated = productMapper.updateById(product);
            if (updated == 0) throw new AppException(40103, "产品已售罄，请重试");
        }
        return coupon;
    }

    @BusinessLog("核销优惠券")
    public Coupon verifyCoupon(String code) {
        Coupon coupon = couponMapper.selectOne(new LambdaQueryWrapper<Coupon>().eq(Coupon::getCode, code).eq(Coupon::getStatus, "available"));
        if (coupon == null) throw new AppException(40106, "卡券无效或已使用");
        coupon.setStatus("used");
        coupon.setUsedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);
        return coupon;
    }
}
