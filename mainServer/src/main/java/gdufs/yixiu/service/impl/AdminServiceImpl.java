package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dao.VolunteerMapper;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.pojo.VolunteerInfo;
import gdufs.yixiu.service.AdminService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.EmailUtils;
import gdufs.yixiu.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private UsersService usersService;
    @Autowired
    private VolunteerMapper volunteerMapper;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EmailUtils emailUtils;

    private String avatarPath;

    @Value("${resources-path.service-avatar-url}")
//    @Value("${resources-path.service-linux-avatar-url}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    @Override
    public String loginByEmail(UsersRegisterDto userDto) {
        Users user = usersMapper.findUserByEmailAndRole(userDto.getEmail(), userDto.getRole());
        if (user == null) {
            user = usersMapper.findSuperAdmin(userDto.getEmail());
            if (user == null) {
                log.info("管理员-{}-未注册", userDto.getEmail());
                return null;
            }
        }
        String token = (String) redisTemplate.opsForValue().get("token:" + user.getUserId());
        if (token != null) {
            log.info("管理员-{}-已在登录状态", userDto.getEmail());
            usersService.updateUserLoginTime(user.getUserId());
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return token;
        }else {
            String newToken = jwtUtils.generateToken(user.getUserId(), user.getRole(), userDto.getVerificationCode());
            redisTemplate.opsForValue().set("token:" + user.getUserId(), newToken, 7, TimeUnit.DAYS);
            usersService.updateUserLoginTime(user.getUserId());
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return newToken;
        }
    }

    @Override
    public String loginByPhone(UsersRegisterDto userDto) {
        return "";
    }

    @Override
    public String sendInviteCode(String email) {
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        redisTemplate.opsForValue().set("inviteCode:email:" + email, code, 24, TimeUnit.HOURS);
        emailUtils.sendCustomVerifyCode(email, "广外义修帮志愿者注册邀请码","【广外义修帮】您的注册邀请码是" + code + "。邀请码有效期为24小时，请尽快登录网站进行注册。如遇邀请码过期，请联系管理员重新发送");
        log.info("邀请码{}已发送至{}",code, email);
        return code;
    }

    @Override
    public Integer modifyVolunteerInfo(VolunteerModifyDto volunteerModifyDto) {
        Users user = new Users();
        user.setUserId(volunteerModifyDto.getUserId());
        user.setRealName(volunteerModifyDto.getRealName());

        VolunteerInfo volunteerInfo = new VolunteerInfo();
        volunteerInfo.setUserId(volunteerModifyDto.getUserId());
        volunteerInfo.setStudentNumber(volunteerModifyDto.getStudentNumber());
        volunteerInfo.setMajorClass(volunteerModifyDto.getMajorClass());
        volunteerInfo.setGrade(volunteerModifyDto.getGrade());
        volunteerInfo.setStatus(volunteerModifyDto.getStatus());

        int basicRow = usersMapper.updateUser(user);
        int volunteerRow = volunteerMapper.updateVolunteerInfo(volunteerInfo);
        return (basicRow + volunteerRow) == 2 ? 1 : 0;
    }
}
