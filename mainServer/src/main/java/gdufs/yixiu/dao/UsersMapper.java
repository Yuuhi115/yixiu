package gdufs.yixiu.dao;

import gdufs.yixiu.dto.VolunteerFilterDto;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.pojo.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UsersMapper {
    int addUserByPhone(Users users);
    int addUserByEmail(Users users);
    int addUserByOpenid(Users users);
    Users findUserById(Integer userId);
    Users findUserByPhoneAndRole(String phone, String role);
    Users findUserByEmailAndRole(String email, String role);
    Users findSuperAdmin(String email);
    int updateUser(Users users);
    List<Integer> findAllUserIds();
    List<Users> findAllVolunteersExcludeMySelf(Integer userId);
    List<Users> findAllVolunteersExcludeMySelfByFilter(VolunteerFilterDto filterDto);
    Users findUserRealNameAndAvatarById(Integer userId);
    List<Users> findUserByName(String name);
    List<UserInfoVO> findUserNameAndAvatarByIds(@Param("userIds") List<Integer> userIds);
}
