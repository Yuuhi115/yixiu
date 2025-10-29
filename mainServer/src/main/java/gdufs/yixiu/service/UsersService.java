package gdufs.yixiu.service;

import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.pojo.Users;

public interface UsersService {
    String registerByPhone(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto, Integer userId);
    Users queryUserByPhoneAndRole(String phone, String role);
}
