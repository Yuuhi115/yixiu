package gdufs.yixiu.component;

import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.pojo.UserFollow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class FollowCacheInit implements CommandLineRunner {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化关注缓存");
        List<UserFollow> userFollows = usersMapper.findAllUserFollows();
        for (UserFollow userFollow : userFollows) {
            Integer followerId = userFollow.getFollowerId();
            Integer followeeId = userFollow.getFolloweeId();

            redisTemplate.opsForSet().add("follow:user:" + followerId, followeeId);
            redisTemplate.opsForSet().add("follow:uploader:" + followeeId, followerId);
        }
        Set<String> keys = redisTemplate.keys("follow:user:*");
        if (keys != null)
            log.info("初始化关注缓存完成，共加载：{}条数据", keys.size());
    }
}
