package gdufs.yixiu.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisConfigInitializer implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TASK_APPROVE_EMAIL_KEY = "config:taskApproveEmail";

    @Override
    public void run(String... args) {
        initializeRedisConfig();
    }

    private void initializeRedisConfig() {
        try {
            Boolean hasKey = redisTemplate.hasKey(TASK_APPROVE_EMAIL_KEY);
            if (hasKey == null || !hasKey) {
                redisTemplate.opsForValue().set(TASK_APPROVE_EMAIL_KEY, "0");
                log.info("初始化Redis配置: {} = 0", TASK_APPROVE_EMAIL_KEY);
            } else {
                String value = redisTemplate.opsForValue().get(TASK_APPROVE_EMAIL_KEY);
                log.info("Redis配置属性已存在，配置正常: {} = {}", TASK_APPROVE_EMAIL_KEY, value);
            }
        } catch (Exception e) {
            log.error("初始化Redis配置失败: {}", e.getMessage(), e);
        }
    }
}

