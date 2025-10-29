package gdufs.yixiu.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import gdufs.yixiu.dto.ClaimsPhoneDto;
import lombok.extern.slf4j.Slf4j;
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

    public String generateTokenByPhone(int id, String phone, String role) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", id);
        claims.put("phone", phone);
        claims.put("role", role);
        return JWT.create()
                .withClaim("claims",claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 一周有效期
                .sign(Algorithm.HMAC256(secretKey));
    }

    public ClaimsPhoneDto getInfoFromToken(String token) {
        Map<String,Object> userMap = JWT.decode(token).getClaim("claims").asMap();
        ClaimsPhoneDto claimsDto = new ClaimsPhoneDto();
        claimsDto.setPhone((String)userMap.get("phone"));
        claimsDto.setId((Integer)userMap.get("id"));
        claimsDto.setRole((String)userMap.get("role"));
        return claimsDto;
    }
}
