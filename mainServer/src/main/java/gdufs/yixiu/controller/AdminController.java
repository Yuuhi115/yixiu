package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.annotation.PassToken;
import gdufs.yixiu.annotation.SuperAdminLoginToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;
import gdufs.yixiu.service.AdminService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private JWTUtils jwtUtils;
    @PassToken
    @PostMapping("loginByEmail")
    public Result loginByEmail(@RequestBody UsersRegisterDto userDto) {
        String token = adminService.loginByEmail(userDto);
        if (token == null) {
            return Result.fail("管理员信息不存在");
        }else {
            return Result.success(token);
        }
    }
    @AdminLoginToken
    @GetMapping("inviteCode")
    public Result sendRegisterInviteCode(@RequestParam String email) {
        String code = adminService.sendInviteCode(email);
        return Result.success(code);
    }
    @SuperAdminLoginToken
    @PutMapping("volunteerInfo")
    public Result modifyVolunteerInfo(@RequestBody VolunteerModifyDto volunteerModifyDto,
                                     HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("管理员 (userId:{}) 正在修改志愿者 (userId:{}) 的信息", userId, volunteerModifyDto.getUserId());
        Integer result = adminService.modifyVolunteerInfo(volunteerModifyDto);
        return result == 1 ? Result.success("更新成功") : Result.fail("更新失败");
    }
}
