package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.VolunteerInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerMapper {
    int addVolunteerInfo(VolunteerInfo volunteer);
    int findIsExistVolunteerByEmail(String email);
    int updateVolunteerInfo(VolunteerInfo volunteer);
    VolunteerInfo findVolunteerInfoByUserId(Integer userId);
}
