package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dao.VolunteerMapper;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerFilterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;
import gdufs.yixiu.dto.community.vo.VolunteerDataVO;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.pojo.VolunteerInfo;
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
public class VolunteerServiceImpl implements VolunteerService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private VolunteerMapper volunteerMapper;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RedisTemplate redisTemplate;

    private String avatarPath;

    @Value("${resources-path.service-avatar-url}")
//    @Value("${resources-path.service-linux-avatar-url}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    @Override
    public String registerByPhone(UsersRegisterDto userDto) {
        return "";
    }

    @Override
    public String loginByPhone(UsersRegisterDto userDto, Integer userId) {
        return "";
    }

    @Override
    public String registerByEmail(UsersRegisterDto userDto) {
        Users user = new Users();
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setUsername("volunteer_" + userDto.getEmail());
        int row = usersMapper.addUserByEmail(user);
        int userId =  user.getUserId();
        VolunteerInfo volunteer = new VolunteerInfo();
        volunteer.setUserId(userId);
        int vtRow = volunteerMapper.addVolunteerInfo(volunteer);
        int vtId = volunteer.getVolunteerId();
        log.info("邮箱志愿者-{}-注册成功, 用户id: {}, 志愿者id: {}", userDto.getEmail(), userId, vtId);
        String token = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
        redisTemplate.opsForValue().set("token:" + userId, token, 7, TimeUnit.DAYS);
        redisTemplate.delete("email_code:" + userDto.getEmail());
        redisTemplate.delete("inviteCode:email:" + userDto.getEmail());
        return token;
    }

    @Override
    public String loginByEmail(UsersRegisterDto userDto, Integer userId) {
        String token = (String) redisTemplate.opsForValue().get("token:" + userId);
        if (token != null) {
            log.info("邮箱志愿者-{}-已在登录状态", userDto.getEmail());
            return token;
        }else {
            String newToken = jwtUtils.generateToken(userId, userDto.getRole(), userDto.getVerificationCode());
            redisTemplate.opsForValue().set("token:" + userId, newToken, 7, TimeUnit.DAYS);
            redisTemplate.delete("email_code:" + userDto.getEmail());
            return newToken;
        }
    }

    @Override
    public Integer isExistVolunteerByEmail(String email) {
        Integer vtNum = volunteerMapper.findIsExistVolunteerByEmail(email);
        log.info("邮箱:{}已注册{}个志愿者",email, vtNum);
        return vtNum;
    }

    @Override
    public void updateVolunteerInfo(VolunteerModifyDto volunteerModifyDto) {
        VolunteerInfo volunteerInfo = new VolunteerInfo();
        volunteerInfo.setUserId(volunteerModifyDto.getUserId());
        volunteerInfo.setStudentNumber(volunteerModifyDto.getStudentNumber());
        volunteerInfo.setMajorClass(volunteerModifyDto.getMajorClass());
        volunteerInfo.setGrade(volunteerModifyDto.getGrade());
        volunteerInfo.setStatus(volunteerModifyDto.getStatus());
        volunteerInfo.setContactType(volunteerModifyDto.getContactType());
        volunteerInfo.setContactNumber(volunteerModifyDto.getContactNumber());
        volunteerMapper.updateVolunteerInfo(volunteerInfo);
    }

    @Override
    public Integer queryVolunteerIdByUserId(Integer userId) {
        return volunteerMapper.findVolunteerIdByUserId(userId);
    }

    @Override
    public Integer queryUserIdByVolunteerId(Integer volunteerId) {
        return volunteerMapper.findUserIdByVolunteerId(volunteerId);
    }

    @Override
    public PageInfo<UserBasicInfoDto> queryVolunteerInfoByName(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        List<Users> volunteers = volunteerMapper.findVolunteersByName(name);
        PageInfo<Users> pageInfo = new PageInfo<>(volunteers);
        List<UserBasicInfoDto> userBasicInfoDtos = new ArrayList<>();
        for (Users volunteer : volunteers){
            UserBasicInfoDto userBasicInfoDto = userToVolunteerBasicInfoDto(volunteer);
            userBasicInfoDtos.add(userBasicInfoDto);
        }
        PageInfo<UserBasicInfoDto> pageInfoResult = new PageInfo<>(userBasicInfoDtos);
        pageInfoResult.setPages(pageInfo.getPages());
        pageInfoResult.setPageNum(pageInfo.getPageNum());
        pageInfoResult.setPageSize(pageInfo.getPageSize());
        pageInfoResult.setTotal(pageInfo.getTotal());
        return pageInfoResult;
    }

    @Override
    public UserBasicInfoDto userToVolunteerBasicInfoDto(Users volunteer) {
        UserBasicInfoDto userBasicInfoDto = new UserBasicInfoDto();
        VolunteerInfo volunteerInfo = volunteerMapper.findVolunteerInfoByUserId(volunteer.getUserId());
        userBasicInfoDto.setUserId(volunteer.getUserId());
        userBasicInfoDto.setUsername(volunteer.getUsername());
        userBasicInfoDto.setRealName(volunteer.getRealName());
        userBasicInfoDto.setPhone(volunteer.getPhone());
        userBasicInfoDto.setEmail(volunteer.getEmail());
        userBasicInfoDto.setAvatar(avatarPath + volunteer.getAvatar());
        userBasicInfoDto.setRole(volunteer.getRole());
        userBasicInfoDto.setStatus(volunteer.getStatus());
        userBasicInfoDto.setLastLogin(volunteer.getLastLogin());
        userBasicInfoDto.setVolunteerInfo(volunteerInfo);
        return userBasicInfoDto;
    }

    @Override
    public PageInfo<UserBasicInfoDto> queryVolunteerListExcludeMyself(Integer pageNum, Integer pageSize, Integer userId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Users> volunteers = usersMapper.findAllVolunteersExcludeMySelf(userId);
        PageInfo<Users> pageInfo = new PageInfo<>(volunteers);
        List<UserBasicInfoDto> userBasicInfoDtos = new ArrayList<>();
        for (Users volunteer : volunteers){
            UserBasicInfoDto userBasicInfoDto = userToVolunteerBasicInfoDto(volunteer);
            userBasicInfoDtos.add(userBasicInfoDto);
        }
        PageInfo<UserBasicInfoDto> pageInfoResult = new PageInfo<>(userBasicInfoDtos);
        pageInfoResult.setPages(pageInfo.getPages());
        pageInfoResult.setPageNum(pageInfo.getPageNum());
        pageInfoResult.setPageSize(pageInfo.getPageSize());
        pageInfoResult.setTotal(pageInfo.getTotal());
        return pageInfoResult;
    }

    @Override
    public PageInfo<UserBasicInfoDto> queryVolunteerListByFilterExcludeMyself(VolunteerFilterDto filterDto, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Users> volunteers = usersMapper.findAllVolunteersExcludeMySelfByFilter(filterDto);
        PageInfo<Users> pageInfo = new PageInfo<>(volunteers);
        List<UserBasicInfoDto> userBasicInfoDtos = new ArrayList<>();
        for (Users volunteer : volunteers){
            UserBasicInfoDto userBasicInfoDto = userToVolunteerBasicInfoDto(volunteer);
            userBasicInfoDtos.add(userBasicInfoDto);
        }
        PageInfo<UserBasicInfoDto> pageInfoResult = new PageInfo<>(userBasicInfoDtos);
        pageInfoResult.setPages(pageInfo.getPages());
        pageInfoResult.setPageNum(pageInfo.getPageNum());
        pageInfoResult.setPageSize(pageInfo.getPageSize());
        pageInfoResult.setTotal(pageInfo.getTotal());
        return pageInfoResult;
    }

    @Override
    public VolunteerDataVO queryVolunteerDataVO(Integer userId) {
        VolunteerDataVO volunteerDataVO = new VolunteerDataVO();
        VolunteerInfo volunteerInfo = volunteerMapper.findVolunteerInfoByUserId(userId);
        if (volunteerInfo == null){
            return null;
        }else {
            volunteerDataVO.setVolunteerId(volunteerInfo.getVolunteerId());
            volunteerDataVO.setGrade(volunteerInfo.getGrade());
            volunteerDataVO.setStatus(volunteerInfo.getStatus());
            volunteerDataVO.setContactType(volunteerInfo.getContactType());
            volunteerDataVO.setContactNumber(volunteerInfo.getContactNumber());
            Map<String, Object> stats = volunteerMapper.getVolunteerRepairStats(volunteerDataVO.getVolunteerId());
            volunteerDataVO.setFixedNum(((Number) stats.get("fixedNum")).intValue());
            volunteerDataVO.setFinishRate(((Number) stats.get("finishRate")).doubleValue());
            return volunteerDataVO;
        }
    }

    @Override
    public VolunteerInfo queryVolunteerInfoByVolunteerId(Integer volunteerId) {
        return volunteerMapper.findVolunteerInfoByVolunteerId(volunteerId);
    }

    @Override
    public List<Users> queryActiveVolunteerList(Integer userId) {
        return volunteerMapper.findActiveVolunteers(userId);
    }
}
