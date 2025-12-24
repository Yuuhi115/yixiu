package gdufs.yixiu.config;

import gdufs.yixiu.interceptor.AuthorizationInterceptor;
import gdufs.yixiu.interceptor.WebInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private WebInterceptor webInterceptor;
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webInterceptor);
        registry.addInterceptor(authorizationInterceptor);
    }

    private String avatarPath;
    @Value("${resources-path.avatar}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    private String requestPath;
    @Value("${resources-path.request}")
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
    private String repairLogPath;
    @Value("${resources-path.repairLog}")
    public void setRepairLogPath(String repairLogPath) {
        this.repairLogPath = repairLogPath;
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("api/v1/img/users/avatar/**").addResourceLocations("file:" + avatarPath);
        registry.addResourceHandler("api/v1/img/task/request/**").addResourceLocations("file:" + requestPath);
        registry.addResourceHandler("api/v1/img/task/repairLog/**").addResourceLocations("file:" + repairLogPath);

//        registry.addResourceHandler("/users/avatar/**").addResourceLocations("file:/usr/yixiuAssist/image/avatar/");
//        registry.addResourceHandler("/task/requestImg/**").addResourceLocations("file:/usr/yixiuAssist/image/request/");
    }
}
