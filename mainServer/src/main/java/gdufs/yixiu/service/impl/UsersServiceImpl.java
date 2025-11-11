package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RedisTemplate redisTemplate;

    private String avatarPath;

    @Value("${resources-path.service-avatar-url}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    @Override
    public String registerByPhone(UsersRegisterDto userDto) {
        Users user = new Users();
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());
        user.setUsername("user_" + userDto.getPhone());
        int row = usersMapper.addUserByPhone(user);
        int userId =  user.getUserId();
        log.info("用户{}注册成功, 用户id: {}", userDto.getPhone(), userId);
        String token = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
        redisTemplate.opsForValue().set("token:" + userId, token, 7, TimeUnit.DAYS);
        return token;
//        }else {
//            log.info("用户{}已注册 -{}- 角色", userDto.getPhone(), userExist.getRole());
//            return jwtUtils.generateTokenByPhone(userExist.getUserId(), userDto.getPhone());
//        }
    }

    @Override
    public String loginByPhone(UsersRegisterDto userDto, Integer userId) {
        return jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
    }

    @Override
    public String registerByEmail(UsersRegisterDto userDto) {
        Users user = new Users();
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setUsername("user_" + userDto.getEmail());
        int row = usersMapper.addUserByEmail(user);
        int userId =  user.getUserId();
        log.info("邮箱用户-{}-注册成功, 用户id: {}", userDto.getEmail(), userId);
        String token = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
        redisTemplate.opsForValue().set("token:" + userId, token, 7, TimeUnit.DAYS);
        return token;
    }

    @Override
    public String loginByEmail(UsersRegisterDto userDto, Integer userId) {
        String token = (String) redisTemplate.opsForValue().get("token:" + userId);
        if (token != null) {
            log.info("邮箱用户-{}-已在登录状态", userDto.getEmail());
            return token;
        }else {
            String newToken = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
            redisTemplate.opsForValue().set("token:" + userId, newToken, 7, TimeUnit.DAYS);
            return newToken;
        }

    }

    @Override
    public Users queryUserByPhoneAndRole(String phone, String role) {
        return usersMapper.findUserByPhoneAndRole(phone, role);
    }

    @Override
    public Users queryUserByEmailAndRole(String email, String role) {
        return usersMapper.findUserByEmailAndRole(email, role);
    }

    @Override
    public void updateUserLoginTime(int userId) {
        Users user = new Users();
        user.setUserId(userId);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        user.setLastLogin(new java.sql.Date(timestamp.getTime()));
        usersMapper.updateUser(user);
    }

    @Override
    public UserBasicInfoDto queryUserById(Integer userId) {
        Users user = usersMapper.findUserById(userId);
        UserBasicInfoDto userBasicInfoDto = new UserBasicInfoDto();
        userBasicInfoDto.setUserId(user.getUserId());
        userBasicInfoDto.setUsername(user.getUsername());
        userBasicInfoDto.setRealName(user.getRealName());
        userBasicInfoDto.setPhone(user.getPhone());
        userBasicInfoDto.setEmail(user.getEmail());
        userBasicInfoDto.setAvatar(avatarPath + user.getAvatar());
        userBasicInfoDto.setRole(user.getRole());
        userBasicInfoDto.setStatus(user.getStatus());
        userBasicInfoDto.setLastLogin(user.getLastLogin());
        return userBasicInfoDto;
    }
}
