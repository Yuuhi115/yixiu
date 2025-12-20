package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.Users;
import org.apache.ibatis.annotations.Mapper;

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
}
