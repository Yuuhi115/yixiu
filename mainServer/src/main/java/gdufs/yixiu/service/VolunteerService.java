package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;

public interface VolunteerService {
    String registerByPhone(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto, Integer userId);
    String registerByEmail(UsersRegisterDto userDto);
    String loginByEmail(UsersRegisterDto userDto, Integer userId);
    Integer isExistVolunteerByEmail(String email);
    void updateVolunteerInfo(VolunteerModifyDto volunteerModifyDto);
    Integer queryVolunteerIdByUserId(Integer userId);
    PageInfo<UserBasicInfoDto> queryVolunteerListExcludeMyself(Integer pageNum, Integer pageSize, Integer userId);
}
