package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.PassToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.ImgUploadService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.MessageUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;
    @Autowired
    private ImgUploadService imgUploadService;
    @Autowired
    private MessageUtils messageUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JWTUtils jwtUtils;

    @PassToken
    @PostMapping("/loginByPhone")
    public Result loginByPhone(@RequestBody UsersRegisterDto userDto) {
//        String verificationCode = (String) redisTemplate.opsForValue().get("phone_code:" + userDto.getPhone());
        Users user = usersService.queryUserByPhoneAndRole(userDto.getPhone(), userDto.getRole());
        if (user == null) {
            log.info("手机用户{}-{}-正在进行注册", userDto.getPhone(), userDto.getRole());
            String token = usersService.registerByPhone(userDto);
            return Result.success(token);
        }else {
            log.info("手机用户{}-{}-正在登录", userDto.getPhone(), user.getRole());
            String token = usersService.loginByPhone(userDto, user.getUserId());
            return Result.success(token);
        }
    }

    @PassToken
    @PostMapping("/loginByEmail")
    public Result loginByEmail(@RequestBody UsersRegisterDto userDto) {
        String verificationCode = (String) redisTemplate.opsForValue().get("email_code:" + userDto.getEmail());
        if (verificationCode == null) {
            return Result.fail("验证码未发送或已过期");
        }
        if (!verificationCode.equals(userDto.getVerificationCode())) {
            return Result.fail("验证码错误");
        }
        Users user = usersService.queryUserByEmailAndRole(userDto.getEmail(), userDto.getRole());
        if (user == null) {
            log.info("邮箱用户{}-{}-正在进行注册", userDto.getEmail(), userDto.getRole());
            String token = usersService.registerByEmail(userDto);
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return Result.success(token);
        }else {
            log.info("邮箱用户{}-{}-正在登录", userDto.getEmail(), user.getRole());
            String token = usersService.loginByEmail(userDto, user.getUserId());
            usersService.updateUserLoginTime(user.getUserId());
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return Result.success(token);
        }
    }

    @UserLoginToken
    @PutMapping("/avatar")
    public Result updateUserAvatar(@RequestParam("avatar") MultipartFile file, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        int id = jwtUtils.getInfoFromToken(token).getId();
        String avatar = imgUploadService.uploadAvatar(file, id);
        return Result.success(avatar);
    }

}
