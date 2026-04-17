package gdufs.yixiu.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import gdufs.yixiu.annotation.*;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.util.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Map;
@Slf4j
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static String secretKey;

    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey) {
        AuthorizationInterceptor.secretKey = secretKey;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        // 如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        //检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        if (method.isAnnotationPresent(SuperAdminLoginToken.class)){
            SuperAdminLoginToken superAdminLoginToken = method.getAnnotation(SuperAdminLoginToken.class);
            if (superAdminLoginToken.required()) {
                if (token == null) {
                    throw new Exception("null token");
                }
            }
            Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
            Integer userId = (Integer) userMap.get("id");
            String redisToken = redisTemplate.opsForValue().get("token:" + userId);
            String role = (String) userMap.get("role");
            if (redisToken == null) {
                throw new Exception("expired token");
            }
            if (!redisToken.equals(token)){
                throw new Exception("this user's token had been replaced");
            }
            if (!"super_admin".equals(role)) {
                throw new Exception("Insufficient Privileges");
            }
//            Users user = usersMapper.findUserById(userId);
//            if (user == null) {
//                throw new Exception("null user");
//            }
//            if (! "super_admin".equals(user.getRole())){
//                throw new Exception("Insufficient Privileges");
//            }
            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            try {
                jwtVerifier.verify(token);
            } catch (Exception e) {
                throw new Exception("token verification failed");
            }
            return true;
        }
        if (method.isAnnotationPresent(AdminLoginToken.class)){
            AdminLoginToken adminLoginToken = method.getAnnotation(AdminLoginToken.class);
            if (adminLoginToken.required()) {
                if (token == null) {
                    throw new Exception("null token");
                }
            }
            Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
            Integer userId = (Integer) userMap.get("id");
            String redisToken = redisTemplate.opsForValue().get("token:" + userId);
            String role = (String) userMap.get("role");
            if (redisToken == null) {
                throw new Exception("expired token");
            }
            if (!redisToken.equals(token)){
                throw new Exception("this user's token had been replaced");
            }
            if (!"admin".equals(role) && !"super_admin".equals(role)) {
                throw new Exception("Insufficient Privileges");
            }
//            Users user = usersMapper.findUserById(userId);
//            if (user == null) {
//                throw new Exception("null user");
//            }
//            if (! "admin".equals(user.getRole()) && ! "super_admin".equals(user.getRole())){
//                throw new Exception("Insufficient Privileges");
//            }
            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            try {
                jwtVerifier.verify(token);
            } catch (Exception e) {
                throw new Exception("token verification failed");
            }
            return true;
        }
        if (method.isAnnotationPresent(VolunteerLoginToken.class)){
            VolunteerLoginToken volunteerLoginToken = method.getAnnotation(VolunteerLoginToken.class);
            if (volunteerLoginToken.required()) {
                if (token == null) {
                    throw new Exception("null token");
                }
            }
            Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
            Integer userId = (Integer) userMap.get("id");
            String redisToken = redisTemplate.opsForValue().get("token:" + userId);
            String role = (String) userMap.get("role");
            if (redisToken == null) {
                throw new Exception("expired token");
            }
            if (!redisToken.equals(token)){
                throw new Exception("this user's token had been replaced");
            }
            if (!"volunteer".equals(role) && !"admin".equals(role) && !"super_admin".equals(role)) {
                throw new Exception("Insufficient Privileges");
            }
//            Users user = usersMapper.findUserById(userId);
//            if (user == null) {
//                throw new Exception("null user");
//            }
//            if (! "volunteer".equals(user.getRole()) &&
//                    !"admin".equals(user.getRole()) &&
//                    !"super_admin".equals(user.getRole())) {
//                throw new Exception("Insufficient Privileges");
//            }
            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            try {
                jwtVerifier.verify(token);
            } catch (Exception e) {
                throw new Exception("token verification failed");
            }
            return true;
        }

        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                if (token == null) {
                    throw new Exception("null token");
                }
                Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
                Integer userId = (Integer) userMap.get("id");
                String redisToken = redisTemplate.opsForValue().get("token:" + userId);
                if (redisToken == null) {
                    throw new Exception("expired token");
                }
                if (!redisToken.equals(token)){
                    throw new Exception("this user's token had been replaced");
                }
//                Users user = usersMapper.findUserById(userId);
//                if (user == null) {
//                    throw new Exception("null user");
//                }
                // 验证 token
                JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
                try {
                    jwtVerifier.verify(token);
                } catch (Exception e) {
                    throw new Exception("token verification failed");
                }
                return true;
            }
        }

        return true;
    }
}
