package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UserModifyDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.community.response.CommunityStatisticDto;
import gdufs.yixiu.dto.community.response.ResponseFollowListDto;
import gdufs.yixiu.pojo.UserFollow;
import gdufs.yixiu.pojo.Users;

import java.util.List;

public interface UsersService {
    String registerByPhone(UsersRegisterDto userDto);
    String loginByPhone(UsersRegisterDto userDto, Integer userId);
    String registerByEmail(UsersRegisterDto userDto);
    String loginByEmail(UsersRegisterDto userDto, Integer userId);
    Users queryUserByPhoneAndRole(String phone, String role);
    Users queryUserByEmailAndRole(String email, String role);
    void updateUserLoginTime(int userId);
    void updateUserBasicInfo(UserModifyDto userBasicInfoDto);
    UserBasicInfoDto queryUserById(Integer userId);
    // 社区
    int addFollow(Integer followerId, Integer followeeId);
    int cancelFollow(Integer followerId, Integer followeeId);
    PageInfo<ResponseFollowListDto> queryFollowList(Integer userId, Integer pageNum, Integer pageSize);
    PageInfo<ResponseFollowListDto> queryFansList(Integer userId, Integer pageNum, Integer pageSize);
    CommunityStatisticDto queryCommunityStatistic(Integer userId);
    List<ResponseFollowListDto> userFollowToResponseFollowListDto(List<UserFollow> userFollowList,
                                                                  List<Integer> queryIds,
                                                                  String type);
}
