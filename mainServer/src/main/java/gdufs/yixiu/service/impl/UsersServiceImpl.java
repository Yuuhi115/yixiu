package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private JWTUtils jwtUtils;

    @Override
    public String registerByPhone(UsersRegisterDto userDto) {
        Users user = new Users();
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());
        user.setUsername("user_" + userDto.getPhone());
        int userId = usersMapper.addUserByPhone(user);
        log.info("用户{}注册成功, 用户id: {}", userDto.getPhone(), userId);
        return jwtUtils.generateTokenByPhone(userId, userDto.getPhone(), userDto.getRole());
//        }else {
//            log.info("用户{}已注册 -{}- 角色", userDto.getPhone(), userExist.getRole());
//            return jwtUtils.generateTokenByPhone(userExist.getUserId(), userDto.getPhone());
//        }
    }

    @Override
    public String loginByPhone(UsersRegisterDto userDto, Integer userId) {
        return jwtUtils.generateTokenByPhone(userId, userDto.getPhone(), userDto.getRole());
    }

    @Override
    public Users queryUserByPhoneAndRole(String phone, String role) {
        return usersMapper.findUserByPhoneAndRole(phone, role);
    }
}
