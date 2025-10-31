package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.Users;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersMapper {
    int addUserByPhone(Users users);
    int addUserByEmail(Users users);
    int addUserByOpenid(Users users);
    Users findUserById(Integer userId);
    Users findUserByPhoneAndRole(String phone, String role);
    Users findUserByEmailAndRole(String email, String role);
    int updateUser(Users users);
}
