package gdufs.yixiu.service;

import gdufs.yixiu.dto.UsersRegisterDto;

public interface AdminService {
    String loginByEmail(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto);
}
