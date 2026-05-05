package com.agritainment.service;

import com.agritainment.common.AppException;
import com.agritainment.entity.*;
import com.agritainment.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final DiningTableMapper tableMapper;
    private final DishMapper dishMapper;
    private final ProductMapper productMapper;
    private final PlotMapper plotMapper;
    private final CameraMapper cameraMapper;
    private final CameraPlotBindingMapper cameraPlotBindingMapper;
    private final MembershipConfigMapper membershipConfigMapper;
    private final UserService userService;

    public Page<User> getUsers(String role, int page, int pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) wrapper.eq(User::getRole, role);
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public User createStaff(String phone, String nickname) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (existing != null) {
            existing.setRole("staff");
            userMapper.updateById(existing);
            return existing;
        }
        User user = new User();
        user.setPhone(phone);
        user.setIdentityCode(userService.generateIdentityCode());
        user.setNickname(nickname);
        user.setRole("staff");
        user.setIsMember(false);
        user.setNoShowCount(0);
        user.setIsBlacklisted(false);
        userMapper.insert(user);
        return user;
    }

    public void deleteStaff(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || !"staff".equals(user.getRole())) throw new AppException(40403, "员工不存在");
        user.setRole("customer");
        userMapper.updateById(user);
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("customerCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "customer")));
        data.put("staffCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "staff")));
        data.put("memberCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getIsMember, true)));
        data.put("tableCount", tableMapper.selectCount(null));
        data.put("productCount", productMapper.selectCount(null));
        data.put("plotCount", plotMapper.selectCount(null));
        data.put("rentedPlotCount", plotMapper.selectCount(new LambdaQueryWrapper<Plot>().eq(Plot::getStatus, "rented")));
        return data;
    }

    public User updateUserStatus(Long id, Boolean isBlacklisted) {
        User user = userMapper.selectById(id);
        if (user == null) throw new AppException(40403, "用户不存在");
        if (isBlacklisted != null) user.setIsBlacklisted(isBlacklisted);
        userMapper.updateById(user);
        return user;
    }

    public Dish createDish(String name, BigDecimal price, String imageUrl, String description, Integer remainingStock) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setPrice(price);
        dish.setImageUrl(imageUrl);
        dish.setDescription(description);
        dish.setRemainingStock(remainingStock);
        dish.setIsAvailable(true);
        dishMapper.insert(dish);
        return dish;
    }

    public Dish updateDish(Long id, String name, BigDecimal price, String imageUrl, String description, Integer remainingStock, Boolean isAvailable) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) throw new AppException(40110, "菜品不存在");
        if (name != null) dish.setName(name);
        if (price != null) dish.setPrice(price);
        if (imageUrl != null) dish.setImageUrl(imageUrl);
        if (description != null) dish.setDescription(description);
        if (remainingStock != null) dish.setRemainingStock(remainingStock);
        if (isAvailable != null) dish.setIsAvailable(isAvailable);
        dishMapper.updateById(dish);
        return dish;
    }

    public void deleteDish(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) throw new AppException(40110, "菜品不存在");
        dishMapper.deleteById(id);
    }

    public Product createProduct(String name, String type, BigDecimal price, BigDecimal memberPrice, String imageUrl, String description, Integer dailyQuota) {
        Product product = new Product();
        product.setName(name);
        product.setType(type);
        product.setPrice(price);
        product.setMemberPrice(memberPrice);
        product.setImageUrl(imageUrl);
        product.setDescription(description);
        product.setDailyQuota(dailyQuota);
        product.setRemainingQuota(dailyQuota != null && dailyQuota != -1 ? dailyQuota : -1);
        product.setIsAvailable(true);
        productMapper.insert(product);
        return product;
    }

    public Product updateProduct(Long id, String name, String type, BigDecimal price, BigDecimal memberPrice, String imageUrl, String description, Integer dailyQuota, Boolean isAvailable) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new AppException(40101, "产品不存在");
        if (name != null) product.setName(name);
        if (type != null) product.setType(type);
        if (price != null) product.setPrice(price);
        if (memberPrice != null) product.setMemberPrice(memberPrice);
        if (imageUrl != null) product.setImageUrl(imageUrl);
        if (description != null) product.setDescription(description);
        if (dailyQuota != null) product.setDailyQuota(dailyQuota);
        if (isAvailable != null) product.setIsAvailable(isAvailable);
        productMapper.updateById(product);
        return product;
    }

    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new AppException(40101, "产品不存在");
        productMapper.deleteById(id);
    }

    public Plot createPlot(String plotNumber, String name, Double area, String description) {
        Plot plot = new Plot();
        plot.setPlotNumber(plotNumber);
        plot.setName(name);
        plot.setArea(area);
        plot.setDescription(description);
        plot.setStatus("available");
        plotMapper.insert(plot);
        return plot;
    }

    public void deletePlot(Long id) {
        Plot plot = plotMapper.selectById(id);
        if (plot == null) throw new AppException(40201, "地块不存在");
        if ("rented".equals(plot.getStatus())) throw new AppException(40202, "地块已被租用，无法删除");
        plotMapper.deleteById(id);
    }
}
