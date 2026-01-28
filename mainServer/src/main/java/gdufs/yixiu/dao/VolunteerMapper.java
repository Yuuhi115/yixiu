package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.pojo.VolunteerInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
    Map<String, Object> getVolunteerRepairStats(@Param("volunteerId") Integer volunteerId);
}
