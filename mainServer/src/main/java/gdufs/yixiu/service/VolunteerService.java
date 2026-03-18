package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerFilterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;
import gdufs.yixiu.dto.community.vo.VolunteerDataVO;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.pojo.VolunteerInfo;

public interface VolunteerService {
    String registerByPhone(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto, Integer userId);
    String registerByEmail(UsersRegisterDto userDto);
    String loginByEmail(UsersRegisterDto userDto, Integer userId);
    Integer isExistVolunteerByEmail(String email);
    void updateVolunteerInfo(VolunteerModifyDto volunteerModifyDto);
    Integer queryVolunteerIdByUserId(Integer userId);
    PageInfo<UserBasicInfoDto> queryVolunteerInfoByName(Integer pageNum, Integer pageSize, String name);
    UserBasicInfoDto userToVolunteerBasicInfoDto(Users user);
    PageInfo<UserBasicInfoDto> queryVolunteerListExcludeMyself(Integer pageNum, Integer pageSize, Integer userId);
    PageInfo<UserBasicInfoDto> queryVolunteerListByFilterExcludeMyself(VolunteerFilterDto filterDto, Integer pageNum, Integer pageSize);
    VolunteerDataVO queryVolunteerDataVO(Integer userId);
    VolunteerInfo queryVolunteerInfoByVolunteerId(Integer volunteerId);
}
