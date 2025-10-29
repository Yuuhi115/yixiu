package gdufs.yixiu.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MessageUtils {
    @Value("${sms.appKey}")
    private String APP_KEY;
    @Value("${sms.appSecret}")
    private String APP_SECRET;
    @Value("${sms.appcode}")
    private String APP_CODE;
    @Value("${sms.host}")
    private String HOST;
    @Value("${sms.path}")
    private String PATH;

    public String sendCode(String phone) throws Exception {
        // 生成6位随机数：100000-999999
        int code = (int)((Math.random() * 900000) + 100000);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + APP_CODE);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> query = new HashMap<String, String>();
        Map<String, String> body = new HashMap<String, String>();
        body.put("mobile", phone);
        body.put("content", "【广外义修帮】您的验证码是" + code + "。如非本人操作，请忽略本短信");

        HttpResponse response = HttpUtils.doPost(HOST, PATH, "POST", headers, query, body);
        log.info("验证码{}已发送至手机号{}", phone, code);
        log.info(response.toString());
        return String.valueOf(code);
    }
}
