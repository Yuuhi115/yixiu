package gdufs.yixiu.util;

import gdufs.yixiu.dto.RepairRequestDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.TaskService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.service.VolunteerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class EmailUtils {

    @Value("${spring.mail.username}")
    private String sendFrom;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private TaskService taskService;

    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(10);

    public String sendVerifyCode(String to, String subject) {
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000)); //生成6位验证码
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
    public void sendCustomVerifyCode(String to, String subject, String content) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sendFrom);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(content);
        mailSender.send(simpleMailMessage);
    }

    public void sendTaskApproveNotify(Integer userId, Integer requestId) {
        List<Users> activeVolunteers = volunteerService.queryActiveVolunteerList(userId);
        if (activeVolunteers == null || activeVolunteers.isEmpty()) {
            log.warn("没有活跃的志愿者，跳过发送邮件通知");
            return;
        }
        RepairRequestDto repairRequestDto = taskService.queryTaskById(requestId);
        if (repairRequestDto == null) {
            log.error("维修任务不存在，requestId: {}", requestId);
            return;
        }
        String campusName = "0".equals(repairRequestDto.getCampus()) ? "大学城校区" : "白云山校区";
        String subject = String.format("【新任务通知】%s - %s %s",
                repairRequestDto.getDeviceType(),
                campusName,
                repairRequestDto.getRepairLocation());

        StringBuilder content = new StringBuilder();
        content.append("【广外义修帮】有新的维修任务已通过审核，请及时查看并接收！\n\n");
        content.append("═══════════════════════════\n");
        content.append("📋 任务编号：").append(repairRequestDto.getRequestId()).append("\n");
        content.append("👤 报修人：").append(repairRequestDto.getRealName()).append("\n");
        content.append("💻 设备类型：").append(repairRequestDto.getDeviceType()).append("\n");
        content.append("📱 设备型号：").append(repairRequestDto.getDeviceModel()).append("\n");
        content.append("🖥️ 设备系统：").append(repairRequestDto.getDeviceSystem()).append("\n");
        content.append("📍 维修地点：").append(campusName).append(" - ").append(repairRequestDto.getRepairLocation()).append("\n");
        content.append("⏰ 预约时间：").append(repairRequestDto.getAppointmentTime()).append("\n");
        content.append("📞 联系方式：").append(repairRequestDto.getContactInfo()).append("\n");

        if (repairRequestDto.getRemarks() != null && !repairRequestDto.getRemarks().isEmpty()) {
            content.append("📝 备注信息：").append(repairRequestDto.getRemarks()).append("\n");
        }

        content.append("\n───────────────────────────────\n");
        content.append("🔧 问题描述：\n");
        content.append(repairRequestDto.getProblemDescription()).append("\n");
        content.append("═══════════════════════════\n\n");
        content.append("请登录系统查看详情并接收任务：https://8.148.253.180:8443/yixiu-site/taskCenter/list\n\n");
        content.append("此邮件为系统自动发送，请勿回复。");

        String finalContent = content.toString();

        for (Users activeVolunteer : activeVolunteers) {
            emailExecutor.submit(() -> {
                try {
                    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
                    simpleMailMessage.setFrom(sendFrom);
                    simpleMailMessage.setSubject(subject);
                    simpleMailMessage.setText(finalContent);
                    simpleMailMessage.setTo(activeVolunteer.getEmail());
                    mailSender.send(simpleMailMessage);
                    log.info("任务审核通过通知已发送至志愿者邮箱: {}", activeVolunteer.getEmail());
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("发送邮件至 {} 失败: {}", activeVolunteer.getEmail(), e.getMessage());
                }
            });
        }
    }
}
