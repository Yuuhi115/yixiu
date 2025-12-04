package gdufs.yixiu.service;

import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;

public interface VolunteerService {
    String registerByPhone(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto, Integer userId);
    String registerByEmail(UsersRegisterDto userDto);
    String loginByEmail(UsersRegisterDto userDto, Integer userId);
    Integer isExistVolunteerByEmail(String email);
    void updateVolunteerInfo(VolunteerModifyDto volunteerModifyDto);
}
