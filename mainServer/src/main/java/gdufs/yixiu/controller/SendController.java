package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.PassToken;
import gdufs.yixiu.util.EmailUtils;
import gdufs.yixiu.util.MessageUtils;
import gdufs.yixiu.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/send")
public class SendController {
    @Autowired
    private MessageUtils messageUtils;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @PassToken
    @GetMapping("/phoneVerification")
    public Result sendPhone(@RequestParam("phone") String phone) throws Exception {
        log.info("用户{}正在获取验证码", phone);
        String code = messageUtils.sendCode(phone);
        return Result.success(code);
    }
    @PassToken
    @GetMapping("/emailVerification")
    public Result sendEmail(@RequestParam("email") String email) throws Exception {
        log.info("用户{}正在获取验证码", email);
        String code = emailUtils.sendVerifyCode(email, "广外义修帮登录/注册验证码");

        String emailCodeKey = "email_code:" + email;
        redisTemplate.opsForValue().set(emailCodeKey, code, 5, TimeUnit.MINUTES);
        return Result.success(code);
    }
}
