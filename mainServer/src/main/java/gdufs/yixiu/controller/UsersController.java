package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.PassToken;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.MessageUtils;
import gdufs.yixiu.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;
    @Autowired
    private MessageUtils messageUtils;

    @PassToken
    @PostMapping("/loginByPhone")
    public Result loginByPhone(@RequestBody UsersRegisterDto userDto) {
        Users user = usersService.queryUserByPhoneAndRole(userDto.getPhone(), userDto.getRole());
        if (user == null) {
            log.info("用户{}-{}-正在进行注册", userDto.getPhone(), userDto.getRole());
            String token = usersService.registerByPhone(userDto);
            return Result.success(token);
        }else {
            log.info("用户{}-{}-正在使用手机号登录", userDto.getPhone(), user.getRole());
            String token = usersService.loginByPhone(userDto, user.getUserId());
            return Result.success(token);
        }
    }
    @PassToken
    @GetMapping("/sendCode")
    public Result sendCode(@RequestParam("phone") String phone) throws Exception {
        log.info("用户{}正在获取验证码", phone);
        String code = messageUtils.sendCode(phone);
        return Result.success(code);
    }
}
