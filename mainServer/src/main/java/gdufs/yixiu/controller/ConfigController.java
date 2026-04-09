package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.SuperAdminLoginToken;
import gdufs.yixiu.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @SuperAdminLoginToken
    @PutMapping("/isSendEmailAboutTaskApprove")
    public Result modifyIsSendEmailAboutTaskApprove(Integer isOpen) {
        redisTemplate.opsForValue().set("config:taskApproveEmail", isOpen.toString());
        log.info("修改是否发送邮件关于任务审核结果为{}", isOpen == 1 ? "是" : "否");
        return Result.success("修改成功");
    }
    @SuperAdminLoginToken
    @GetMapping("/isSendEmailAboutTaskApprove")
    public Result queryIsSendEmailAboutTaskApprove() {
        String value = redisTemplate.opsForValue().get("config:taskApproveEmail");
        if (value == null) {
            log.warn("Redis中不存在配置: config:taskApproveEmail，使用默认值0");
            value = "0";
        }
        Integer result = Integer.valueOf(value);
        return Result.success(result);
    }
}
