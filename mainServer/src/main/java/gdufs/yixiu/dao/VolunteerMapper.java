package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.pojo.VolunteerInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VolunteerMapper {
    int addVolunteerInfo(VolunteerInfo volunteer);
    int findIsExistVolunteerByEmail(String email);
    int updateVolunteerInfo(VolunteerInfo volunteer);
    VolunteerInfo findVolunteerInfoByUserId(Integer userId);
    VolunteerInfo findVolunteerInfoByVolunteerId(Integer volunteerId);
    String findVolunteerNameByUserId(Integer userId);
    String findVolunteerNameByVolunteerId(Integer volunteerId);
    Integer findVolunteerIdByUserId(Integer userId);
    List<Users> findVolunteersByName(String name);
}
