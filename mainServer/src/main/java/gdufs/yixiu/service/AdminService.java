package gdufs.yixiu.service;

import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;

public interface AdminService {
    String loginByEmail(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto);
    String sendInviteCode(String email);
    Integer modifyVolunteerInfo(VolunteerModifyDto volunteerModifyDto);
}
