package gdufs.yixiu.dao;

import gdufs.yixiu.dto.UserCommentListDto;
import gdufs.yixiu.dto.UserListDto;
import gdufs.yixiu.dto.VolunteerFilterDto;
import gdufs.yixiu.dto.community.request.FollowListFilterDto;
import gdufs.yixiu.dto.community.response.CommunityStatisticDto;
import gdufs.yixiu.dto.community.response.ResponseFollowListDto;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.dto.community.vo.VolunteerDataVO;
import gdufs.yixiu.dto.filter.UserCommentListFilter;
import gdufs.yixiu.dto.filter.UserListFilter;
import gdufs.yixiu.pojo.UserFollow;
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
    UserInfoVO findUserInfoVOById(Integer userId);

    // 社区
    int addUserFollow(UserFollow userFollow);
    int isExistUserFollow(UserFollow userFollow);
    int isExistUserFollowing(UserFollow userFollow);
    int updateUserFollow(UserFollow userFollow);
    // 获取关注列表
    List<UserFollow> findFolloweesByFilter(FollowListFilterDto filterDto);
    // 获取粉丝列表
    List<UserFollow> findFollowersByFilter(FollowListFilterDto filterDto);
    CommunityStatisticDto findCommunityStatistic(Integer userId);
    List<UserFollow> findAllUserFollows();
    int upsertProfileView(@Param("viewerId") Integer viewerId,
                          @Param("userId") Integer userId,
                          @Param("ip") String ip);
    Integer findProfileViewCount(@Param("viewedId") Integer viewedId);

    //用户列表
    List<UserListDto> findUserListByFilter(UserListFilter userListFilter);
    List<UserCommentListDto> findUserCommentListByFilter(UserCommentListFilter userCommentListFilter);
}
