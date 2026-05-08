package com.agritainment.service;

import com.agritainment.annotation.BusinessLog;
import com.agritainment.common.AppException;
import com.agritainment.entity.User;
import com.agritainment.enums.RoleEnum;
import com.agritainment.mapper.UserMapper;
import com.agritainment.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Value("${app.sms.dev-code:123456}")
    private String devSmsCode;

    private final Map<String, SmsCodeEntry> smsCodeStore = new ConcurrentHashMap<>();

    public void sendSmsCode(String phone) {
        if (phone == null || phone.length() != 11) throw new AppException(40401, "手机号格式不正确");
        String code = String.valueOf(100000 + ThreadLocalRandom.current().nextInt(900000));
        smsCodeStore.put(phone, new SmsCodeEntry(code, LocalDateTime.now().plusMinutes(5)));
    }

    private boolean verifySmsCode(String phone, String code) {
        if (devSmsCode.equals(code)) return true;
        SmsCodeEntry entry = smsCodeStore.get(phone);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expireAt)) {
            smsCodeStore.remove(phone);
            return false;
        }
        boolean valid = entry.code.equals(code);
        if (valid) smsCodeStore.remove(phone);
        return valid;
    }

    public String register(String phone, String code) {
        if (phone == null || code == null) throw new AppException(40401, "手机号和验证码不能为空");
        if (!verifySmsCode(phone, code)) throw new AppException(40405, "验证码错误或已过期");
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (count > 0) throw new AppException(40402, "手机号已注册");
        User user = new User();
        user.setPhone(phone);
        user.setIdentityCode(userService.generateIdentityCode());
        user.setRole(RoleEnum.CUSTOMER);
        user.setIsMember(false);
        user.setNoShowCount(0);
        user.setIsBlacklisted(false);
        userMapper.insert(user);
        return jwtUtil.generateToken(user.getId(), phone, RoleEnum.CUSTOMER, false);
    }

    public String login(String phone, String code) {
        if (phone == null || code == null) throw new AppException(40401, "手机号和验证码不能为空");
        if (!verifySmsCode(phone, code)) throw new AppException(40405, "验证码错误或已过期");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) throw new AppException(40403, "用户不存在");
        return jwtUtil.generateToken(user.getId(), phone, user.getRole(), user.getIsMember());
    }

    public String adminLogin(String phone, String password) {
        if (phone == null || password == null) throw new AppException(40401, "手机号和密码不能为空");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone).eq(User::getRole, RoleEnum.ADMIN));
        if (user == null) throw new AppException(40403, "管理员账号不存在");
        if (user.getPassword() == null || !BCrypt.checkpw(password, user.getPassword()))
            throw new AppException(40404, "密码错误");
        return jwtUtil.generateToken(user.getId(), phone, RoleEnum.ADMIN, false);
    }

    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new AppException(40403, "用户不存在");
        user.setPassword(null);
        return user;
    }

    @BusinessLog("绑定微信OpenID")
    public void bindOpenid(Long userId, String openid) {
        if (userId == null || openid == null) return;
        User user = userMapper.selectById(userId);
        if (user != null && !openid.equals(user.getOpenid())) {
            user.setOpenid(openid);
            userMapper.updateById(user);
        }
    }

    private record SmsCodeEntry(String code, LocalDateTime expireAt) {}
}
