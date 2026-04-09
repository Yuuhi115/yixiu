package gdufs.yixiu.controller;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.annotation.PassToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.annotation.VolunteerLoginToken;
import gdufs.yixiu.dao.TaskMapper;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerFilterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.ImgUploadService;
import gdufs.yixiu.service.TaskService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.service.VolunteerService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.MessageUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/volunteer")
public class VolunteerController {
    @Autowired
    private UsersService usersService;
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ImgUploadService imgUploadService;
    @Autowired
    private MessageUtils messageUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private TaskMapper taskMapper;

    @PassToken
    @PostMapping("/loginByEmail")
    public Result loginByEmail(@RequestBody UsersRegisterDto userDto) {

        String verificationCode = (String) redisTemplate.opsForValue().get("email_code:" + userDto.getEmail());
        if (verificationCode == null) {
            return Result.fail("验证码未发送或已过期");
        }
        if (!verificationCode.equals(userDto.getVerificationCode())) {
            return Result.fail("验证码错误");
        }
        Users user = usersService.queryUserByEmailAndRole(userDto.getEmail(), userDto.getRole());
        if (user == null) {
            return Result.fail("该邮箱未注册");
        }else {
            log.info("邮箱用户{}-{}-正在登录", userDto.getEmail(), user.getRole());
            String token = volunteerService.loginByEmail(userDto, user.getUserId());
            usersService.updateUserLoginTime(user.getUserId());
            return Result.success(token);
        }
    }

    @PassToken
    @PostMapping("/registerByEmail")
    public Result registerByEmail(@RequestBody UsersRegisterDto userDto) {

        String inviteCode = (String) redisTemplate.opsForValue().get("inviteCode:email:" + userDto.getEmail());
        if (inviteCode == null) {
            return Result.fail("该邮箱没有邀请码，请更换账户");
        }
        if (!inviteCode.equals(userDto.getInviteCode())) {
            return Result.fail("邀请码错误");
        }

        String verificationCode = (String) redisTemplate.opsForValue().get("email_code:" + userDto.getEmail());
        if (verificationCode == null) {
            return Result.fail("验证码未发送或已过期");
        }
        if (!verificationCode.equals(userDto.getVerificationCode())) {
            return Result.fail("验证码错误");
        }
        Integer num = volunteerService.isExistVolunteerByEmail(userDto.getEmail());
        if (num > 0){
            return Result.fail("该邮箱已注册志愿者/管理员账户");
        }
        log.info("邮箱用户{}-{}-正在进行注册", userDto.getEmail(), userDto.getRole());
        String token = volunteerService.registerByEmail(userDto);
        return Result.success(token);
    }
    @VolunteerLoginToken
    @PutMapping("/info")
    public Result modifyInfo(@RequestBody VolunteerModifyDto volunteerModifyDto,
                             HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        int id = jwtUtils.getInfoFromToken(token).getId();
        volunteerModifyDto.setUserId(id);
        log.info("志愿者:{} 正在修改信息", volunteerModifyDto.getUserId());
        volunteerService.updateVolunteerInfo(volunteerModifyDto);
        log.info("志愿者:{} 更新信息为{}", volunteerModifyDto.getUserId(), volunteerModifyDto);
        return Result.success("更新成功");
    }
    @VolunteerLoginToken
    @GetMapping("/infoListExcludeUserId")
    public Result queryVolunteerListExcludeMyself(@RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum,
                                                 @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize,
                                                 HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        PageInfo<UserBasicInfoDto> pageInfo = volunteerService.queryVolunteerListExcludeMyself(pageNum, pageSize, userId);
        return Result.success(pageInfo);
    }
    @VolunteerLoginToken
    @GetMapping("/infoListByFilterExcludeUserId")
    public Result queryVolunteerListByFilterExcludeMyself(VolunteerFilterDto filterDto,
                                                          @RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum,
                                                          @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize,
                                                          HttpServletRequest request) {
//        log.info("接收到的筛选参数:{}-{}-{}", filterDto, pageNum, pageSize);
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        filterDto.setExcludeUserId(userId);
        PageInfo<UserBasicInfoDto> pageInfo = volunteerService.queryVolunteerListByFilterExcludeMyself(filterDto, pageNum, pageSize);
        return Result.success(pageInfo);
    }
    @VolunteerLoginToken
    @GetMapping("/infoListByName")
    public Result queryVolunteerListByName(@RequestParam(defaultValue = "1", name = "pageNum") Integer pageNum,
                                          @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize,
                                          @RequestParam(defaultValue = "", name = "name") String name,
                                          HttpServletRequest request) {
        PageInfo<UserBasicInfoDto> pageInfo = volunteerService.queryVolunteerInfoByName(pageNum, pageSize, name);
        return Result.success(pageInfo);
    }
    @UserLoginToken
    @GetMapping("/skillListBySkillId")
    public Result querySkillListBySkillId(@RequestParam(name = "skillId") Integer skillId,
                                         HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("志愿者(user_id:{})正在查询技能id为{}的技能列表",userId, skillId);
        return Result.success(taskService.queryVolunteerSkillBySkillId(skillId));
    }
}
