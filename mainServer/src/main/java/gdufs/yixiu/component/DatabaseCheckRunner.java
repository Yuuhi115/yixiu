package gdufs.yixiu.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
@Slf4j
@Component
public class DatabaseCheckRunner implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                log.info("Mysql数据库连接正常");
            }
        } catch (Exception e) {
            log.error("数据库连接失败: " + e.getMessage());
            // 可以选择退出应用
            System.exit(1);
        }
    }
}
