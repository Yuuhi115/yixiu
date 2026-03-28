package gdufs.yixiu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class AESUtils {
    private static String KEY;
    @Value("${aes.key}")
    public void setKey(String key) {
        AESUtils.KEY = key;
    }
    private static String IV;
    @Value("${aes.iv}")
    public void setIv(String iv) {
        AESUtils.IV = iv;
    }

    public static String decrypt(String content) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKeySpec keySpec =
                new SecretKeySpec(KEY.getBytes(), "AES");

        IvParameterSpec iv =
                new IvParameterSpec(IV.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

        byte[] decoded =
                Base64.getDecoder().decode(content);

        byte[] result = cipher.doFinal(decoded);

        return new String(result);
    }

    public static String encrypt(String content) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKeySpec keySpec =
                new SecretKeySpec(KEY.getBytes(), "AES");

        IvParameterSpec iv =
                new IvParameterSpec(IV.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

        byte[] encrypted = cipher.doFinal(content.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);
    }
}
