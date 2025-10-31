package gdufs.yixiu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailUtils {
    private String code;
    @Value("${spring.mail.username}")
    private String sendFrom;
    @Autowired
    private JavaMailSender mailSender;

    public String sendVerifyCode(String to, String subject) {
        code = String.valueOf((int)((Math.random() * 9 + 1) * 100000)); //生成6位验证码
        String content = "【广外义修帮】您的验证码是" + code + "。如非本人操作，请忽略本邮件";
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sendFrom);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(content);
        mailSender.send(simpleMailMessage);
        log.info("验证码{}已发送至{}",code, to);
        return code;
    }
}
