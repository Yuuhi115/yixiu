package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.AdminService;
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
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private UsersService usersService;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RedisTemplate redisTemplate;

    private String avatarPath;

    @Value("${resources-path.service-avatar-url}")
//    @Value("${resources-path.service-linux-avatar-url}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    @Override
    public String loginByEmail(UsersRegisterDto userDto) {
        Users user = usersMapper.findUserByEmailAndRole(userDto.getEmail(), userDto.getRole());
        if (user == null) {
            user = usersMapper.findSuperAdmin(userDto.getEmail());
            if (user == null) {
                log.info("管理员-{}-未注册", userDto.getEmail());
                return null;
            }
        }
        String token = (String) redisTemplate.opsForValue().get("token:" + user.getUserId());
        if (token != null) {
            log.info("管理员-{}-已在登录状态", userDto.getEmail());
            usersService.updateUserLoginTime(user.getUserId());
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return token;
        }else {
            String newToken = jwtUtils.generateToken(user.getUserId(), user.getRole(), userDto.getVerificationCode());
            redisTemplate.opsForValue().set("token:" + user.getUserId(), newToken, 7, TimeUnit.DAYS);
            usersService.updateUserLoginTime(user.getUserId());
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return newToken;
        }
    }

    @Override
    public String loginByPhone(UsersRegisterDto userDto) {
        return "";
    }
}
