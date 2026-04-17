package gdufs.yixiu.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import gdufs.yixiu.dto.ClaimsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Component
public class JWTUtils {

    private static String secretKey;

    @Value("${jwt.secret}")
    public void setSecretKey(String secretKey) {
        JWTUtils.secretKey = secretKey;
    }

    public String generateToken(int id, String role, String verificationCode, String ip) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", id);
        claims.put("role", role);
        claims.put("verificationCode", verificationCode);
        claims.put("ip", ip);
        return JWT.create()
                .withClaim("claims",claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 一周有效期
                .sign(Algorithm.HMAC256(secretKey));
    }
//    public String generateTokenByEmail(int id, String email, String role) {
//        Map<String, Object> claims = new HashMap<String, Object>();
//        claims.put("id", id);
//        claims.put("email", email);
//        claims.put("role", role);
//        return JWT.create()
//                .withClaim("claims",claims)
//                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 一周有效期
//                .sign(Algorithm.HMAC256(secretKey));
//    }

    public ClaimsDto getInfoFromToken(String token) {
        Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
        ClaimsDto claimsDto = new ClaimsDto();
        claimsDto.setId((Integer)userMap.get("id"));
        claimsDto.setRole((String)userMap.get("role"));
        claimsDto.setIp((String)userMap.get("ip"));
        return claimsDto;
    }
//    public ClaimsDto getInfoFromTokenByEmail(String token) {
//        Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
//        ClaimsDto claimsDto = new ClaimsDto();
//        claimsDto.setEmail((String)userMap.get("email"));
//        claimsDto.setId((Integer)userMap.get("id"));
//        claimsDto.setRole((String)userMap.get("role"));
//        return claimsDto;
//    }
}
