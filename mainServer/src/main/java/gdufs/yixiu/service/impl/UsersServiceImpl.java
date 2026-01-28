package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.PostMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dao.VolunteerMapper;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UserModifyDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.community.request.FollowListFilterDto;
import gdufs.yixiu.dto.community.response.CommunityStatisticDto;
import gdufs.yixiu.dto.community.response.ProfileDto;
import gdufs.yixiu.dto.community.response.ResponseFollowListDto;
import gdufs.yixiu.dto.community.vo.LikeListIdsVO;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.UserFollow;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.pojo.VolunteerInfo;
import gdufs.yixiu.service.CommentService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.service.VolunteerService;
import gdufs.yixiu.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private VolunteerMapper volunteerMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentService commentService;
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String avatarPath;

    @Value("${resources-path.service-avatar-url}")
//    @Value("${resources-path.service-linux-avatar-url}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    @Override
    public String registerByPhone(UsersRegisterDto userDto) {
        Users user = new Users();
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());
        user.setUsername("user_" + userDto.getPhone());
        int row = usersMapper.addUserByPhone(user);
        int userId =  user.getUserId();
        log.info("用户{}注册成功, 用户id: {}", userDto.getPhone(), userId);
        String token = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
        redisTemplate.opsForValue().set("token:" + userId, token, 7, TimeUnit.DAYS);
        return token;
//        }else {
//            log.info("用户{}已注册 -{}- 角色", userDto.getPhone(), userExist.getRole());
//            return jwtUtils.generateTokenByPhone(userExist.getUserId(), userDto.getPhone());
//        }
    }

    @Override
    public String loginByPhone(UsersRegisterDto userDto, Integer userId) {
        return jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
    }

    @Override
    public String registerByEmail(UsersRegisterDto userDto) {
        Users user = new Users();
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setUsername("user_" + userDto.getEmail());
        int row = usersMapper.addUserByEmail(user);
        int userId =  user.getUserId();
        log.info("邮箱用户-{}-注册成功, 用户id: {}", userDto.getEmail(), userId);
        String token = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
        redisTemplate.opsForValue().set("token:" + userId, token, 7, TimeUnit.DAYS);
        return token;
    }

    @Override
    public String loginByEmail(UsersRegisterDto userDto, Integer userId) {
        String token = (String) redisTemplate.opsForValue().get("token:" + userId);
        if (token != null) {
            log.info("邮箱用户-{}-已在登录状态", userDto.getEmail());
            return token;
        }else {
            String newToken = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
            redisTemplate.opsForValue().set("token:" + userId, newToken, 7, TimeUnit.DAYS);
            return newToken;
        }

    }

    @Override
    public Users queryUserByPhoneAndRole(String phone, String role) {
        return usersMapper.findUserByPhoneAndRole(phone, role);
    }

    @Override
    public Users queryUserByEmailAndRole(String email, String role) {
        return usersMapper.findUserByEmailAndRole(email, role);
    }

    @Override
    public void updateUserLoginTime(int userId) {
        Users user = new Users();
        user.setUserId(userId);
        java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        user.setLastLogin(new java.sql.Date(timestamp.getTime()));
        usersMapper.updateUser(user);
    }

    @Override
    public void updateUserBasicInfo(UserModifyDto userModifyDto) {
        Users user = new Users();
        user.setUserId(userModifyDto.getUserId());
        user.setUsername(userModifyDto.getUsername());
        user.setRealName(userModifyDto.getRealName());
        user.setRole(userModifyDto.getRole());
        usersMapper.updateUser(user);
    }

    @Override
    public UserBasicInfoDto queryUserById(Integer userId) {
        Users user = usersMapper.findUserById(userId);
        UserBasicInfoDto userBasicInfoDto = new UserBasicInfoDto();
        userBasicInfoDto.setUserId(user.getUserId());
        userBasicInfoDto.setUsername(user.getUsername());
        userBasicInfoDto.setRealName(user.getRealName());
        userBasicInfoDto.setPhone(user.getPhone());
        userBasicInfoDto.setEmail(user.getEmail());
        userBasicInfoDto.setAvatar(avatarPath + user.getAvatar());
        userBasicInfoDto.setUserSignature(user.getUserSignature());
        userBasicInfoDto.setRole(user.getRole());
        userBasicInfoDto.setStatus(user.getStatus());
        userBasicInfoDto.setLastLogin(user.getLastLogin());
        VolunteerInfo volunteerInfo = volunteerMapper.findVolunteerInfoByUserId(userId);
        userBasicInfoDto.setVolunteerInfo(volunteerInfo);
        return userBasicInfoDto;
    }

    @Override
    public int addFollow(Integer followerId, Integer followeeId) {
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFolloweeId(followeeId);
        int isFollowed = usersMapper.isExistUserFollow(userFollow);
        int result;
        if (isFollowed == 0) {
            result = usersMapper.addUserFollow(userFollow);
        }else {
            userFollow.setStatus(1);
            result = usersMapper.updateUserFollow(userFollow);
        }
        redisTemplate.opsForSet().add("follow:user:" + followerId, followeeId);
        redisTemplate.opsForSet().add("follow:uploader:" + followeeId, followerId);
        return result;
    }

    @Override
    public int cancelFollow(Integer followerId, Integer followeeId) {
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFolloweeId(followeeId);
        int isFollowed = usersMapper.isExistUserFollow(userFollow);
        if (isFollowed == 0) {
            return 0;
        }
        userFollow.setStatus(0);
        if (usersMapper.updateUserFollow(userFollow) == 1) {
            redisTemplate.opsForSet().remove("follow:user:" + followerId, followeeId);
            redisTemplate.opsForSet().remove("follow:uploader:" + followeeId, followerId);
            return 1;
        }
        return 0;
    }

    @Override
    public List<ResponseFollowListDto> userFollowToResponseFollowListDto(List<UserFollow> userFollowList,
                                                                         List<Integer> queryIds, String type) {
        Map<Integer, UserInfoVO> followUserInfoMap = commentService.getUserInfoMap(queryIds);
        List<ResponseFollowListDto> responseFollowListDtos = new ArrayList<>();
        for (UserFollow userFollow : userFollowList) {
            ResponseFollowListDto responseFollowListDto = new ResponseFollowListDto();
            responseFollowListDto.setFollowId(userFollow.getFollowId());
            UserInfoVO userInfoVO;
            if (type.equals("follow")) {
                responseFollowListDto.setFollowUserId(userFollow.getFolloweeId());
                userInfoVO = followUserInfoMap.get(userFollow.getFolloweeId());
            }else {
                responseFollowListDto.setFollowUserId(userFollow.getFollowerId());
                userInfoVO = followUserInfoMap.get(userFollow.getFollowerId());
            }
            responseFollowListDto.setFollowUsername(userInfoVO.getUsername());
            responseFollowListDto.setFollowUserAvatar(avatarPath + userInfoVO.getAvatar());
            responseFollowListDto.setFollowUserSignature(userInfoVO.getUserSignature());
            responseFollowListDto.setStatus(userFollow.getStatus());
            responseFollowListDto.setCreateTime(userFollow.getCreateTime());
            responseFollowListDto.setUpdateTime(userFollow.getUpdateTime());
            responseFollowListDtos.add(responseFollowListDto);
        }
        return responseFollowListDtos;
    }

    @Override
    public UserInfoVO queryUserInfoVOById(Integer userId) {
        Users user = usersMapper.findUserById(userId);
        return new UserInfoVO(user.getUserId(), user.getUsername(), user.getAvatar(), user.getUserSignature());
    }

    @Override
    public int addProfileView(Integer viewerId, Integer userId, String ip) {
        return usersMapper.upsertProfileView(viewerId, userId, ip);
    }

    @Override
    public ProfileDto queryProfileDtoByUserId(Integer userId, Integer viewerId) {
        UserFollow userFollow = new UserFollow();
        Users user = usersMapper.findUserById(userId);
        userFollow.setFollowerId(viewerId);
        userFollow.setFolloweeId(userId);
        int isFollowed = usersMapper.isExistUserFollowing(userFollow);
        ProfileDto profileDto = new ProfileDto();
        profileDto.setCommunityStatisticDto(queryCommunityStatistic(userId));
        UserInfoVO userInfoVO = queryUserInfoVOById(userId);
        String avatar = avatarPath + user.getAvatar();
        userInfoVO.setAvatar(avatar);
        profileDto.setUserInfoVO(userInfoVO);
        profileDto.setIsFollow(isFollowed == 1);
        profileDto.setLastLoginTime(user.getLastLogin());
        profileDto.setVolunteerDataVO(volunteerService.queryVolunteerDataVO(userId));
        profileDto.setVisitedNum(usersMapper.findProfileViewCount(userId));
        profileDto.setRole(user.getRole());
        return profileDto;
    }

    @Override
    public PageInfo<ResponseFollowListDto> queryFollowListByFilter(FollowListFilterDto filterDto, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserFollow> userFollowList = usersMapper.findFolloweesByFilter(filterDto);
        PageInfo<UserFollow> userFollowPageInfo = new PageInfo<>(userFollowList);
        List<Integer> followeeIds = userFollowList.stream()
                .map(UserFollow::getFolloweeId)
                .distinct() // 去重，避免重复查询
                .toList();
        List<ResponseFollowListDto> responseFollowList = userFollowToResponseFollowListDto(userFollowList, followeeIds, "follow");
        PageInfo<ResponseFollowListDto> resultPageInfo = new PageInfo<>(responseFollowList);
        resultPageInfo.setTotal(userFollowPageInfo.getTotal());
        resultPageInfo.setPages(userFollowPageInfo.getPages());
        resultPageInfo.setPageNum(userFollowPageInfo.getPageNum());
        resultPageInfo.setPageSize(userFollowPageInfo.getPageSize());
        return resultPageInfo;
    }

    @Override
    public PageInfo<ResponseFollowListDto> queryFansList(FollowListFilterDto filterDto, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserFollow> userFollowList = usersMapper.findFollowersByFilter(filterDto);
        PageInfo<UserFollow> userFollowPageInfo = new PageInfo<>(userFollowList);
        List<Integer> followerIds = userFollowList.stream()
                .map(UserFollow::getFollowerId)
                .distinct() // 去重，避免重复查询
                .toList();
        List<ResponseFollowListDto> responseFollowList = userFollowToResponseFollowListDto(userFollowList, followerIds, "fans");
        PageInfo<ResponseFollowListDto> resultPageInfo = new PageInfo<>(responseFollowList);
        resultPageInfo.setTotal(userFollowPageInfo.getTotal());
        resultPageInfo.setPages(userFollowPageInfo.getPages());
        resultPageInfo.setPageNum(userFollowPageInfo.getPageNum());
        resultPageInfo.setPageSize(userFollowPageInfo.getPageSize());
        return resultPageInfo;
    }

    @Override
    public CommunityStatisticDto queryCommunityStatistic(Integer userId) {
        CommunityStatisticDto communityStatisticDto = usersMapper.findCommunityStatistic(userId);
        communityStatisticDto.setUserId(userId);
        List<Integer> postIds = postMapper.getPostIdsByUserId(userId);
        List<Integer> commentIds = postMapper.getCommentIdsByUserId(userId);
        List<Integer> replyIds = postMapper.getReplyIdsByUserId(userId);
        LikeListIdsVO likeListIdsVO = new LikeListIdsVO(postIds, commentIds, replyIds);
        int likeCount = postMapper.getPostsAndCommentsLikeCount(likeListIdsVO);
        communityStatisticDto.setGetLikeNum(likeCount);
        return communityStatisticDto;
    }

}
