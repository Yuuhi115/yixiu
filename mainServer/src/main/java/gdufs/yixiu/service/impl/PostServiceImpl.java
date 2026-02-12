package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.CommentMapper;
import gdufs.yixiu.dao.PostMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.community.request.PostFilterDto;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.response.PostFavoriteInfoDto;
import gdufs.yixiu.dto.community.response.ResponsePostDto;
import gdufs.yixiu.dto.community.response.ResponseReplyDto;
import gdufs.yixiu.dto.community.vo.PostCommentStatisticVO;
import gdufs.yixiu.dto.community.vo.PostIdJudgeVO;
import gdufs.yixiu.dto.community.vo.TagVO;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.pojo.Notification;
import gdufs.yixiu.pojo.Post;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.CommentService;
import gdufs.yixiu.service.ImgUploadService;
import gdufs.yixiu.service.NotificationService;
import gdufs.yixiu.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ImgUploadService imgUploadService;
    @Autowired
    private NotificationService notificationService;
    private String serviceCommunityUrl;
    @Value("${resources-path.service-community-url}")
    public void setServiceCommunityUrl(String serviceCommunityUrl) {
        this.serviceCommunityUrl = serviceCommunityUrl;
    }
    private String serviceUserUrl;
    @Value("${resources-path.service-avatar-url}")
    public void setServiceUserUrl(String serviceUserUrl) {
        this.serviceUserUrl = serviceUserUrl;
    }
    @Override
    public int createPost(RequestPostDto requestPostDto) {
        Post post = new Post();
        post.setUserId(requestPostDto.getUserId());
        post.setTitle(requestPostDto.getTitle());
        post.setContent(requestPostDto.getContent());
        int row = postMapper.addPost(post);
        if (row == 1) {
            Set<Object> fans = redisTemplate.opsForSet()
                    .members("follow:uploader:" + post.getUserId());
            if (fans != null && !fans.isEmpty()) {
                for (Object fanId : fans) {
                    redisTemplate.opsForSet()
                            .add("follow:update:" + fanId, post.getUserId());
                }
            }
        }
        log.info("用户(id:{})上传帖子成功，帖子id为：{}，帖子标签为{}",post.getUserId(), post.getPostId(), requestPostDto.getTagIdList());
        if (requestPostDto.getTagIdList() != null && !requestPostDto.getTagIdList().isEmpty()) {
            postMapper.addPostTags(post.getPostId(), requestPostDto.getTagIdList());
        }
        return row == 1 ? post.getPostId() : 0;
    }

    @Override
    public int deletePost(Integer postId, Integer userId, String role) {
        Post post = postMapper.queryPostById(postId);
        if (post == null) {
            return 500;
        }
        if (role.equals("admin") || role.equals("super_admin")){
            imgUploadService.deletePostImg(postId);
            int row = postMapper.deletePost(postId);
            if (row == 1){
                log.info("管理员(id:{})删除帖子成功，帖子id为：{}",userId, postId);
                if (!post.getUserId().equals(userId)){
                    Notification notification = new Notification();
                    notification.setSenderId(userId);
                    notification.setReceiverId(post.getUserId());
                    notification.setTitle("帖子删除通知");
                    notification.setContent("您的帖子(post_id: " + postId + ", Title: " + post.getTitle() + ")已被管理员删除，详情请联系管理员");
                    notification.setType("USER");
                    notificationService.saveAndPush(notification);
                }
                return 200;
            }else {
                log.info("管理员(id:{})删除帖子失败，帖子id为：{}",userId, postId);
                return 500;
            }
        }
        if (!post.getUserId().equals(userId)) {
            return 403;
        }
        int row = postMapper.deletePost(postId);
        if (row == 1){
            log.info("用户(id:{})删除帖子成功，帖子id为：{}",userId, postId);
            imgUploadService.deletePostImg(postId);
            return 200;
        }else {
            log.info("用户(id:{})删除帖子失败，帖子id为：{}",userId, postId);
            return 500;
        }
    }

    @Override
    public ResponsePostDto getPostByPostId(Integer postId, Integer userId) {
        Post post = postMapper.queryPostById(postId);
        List<Post> postList = new ArrayList<>();
        postList.add(post);
        List<ResponsePostDto> responsePostDtos = getPostsDetail(postList, userId);
        return responsePostDtos.getFirst();
    }

    @Override
    public PageInfo<ResponsePostDto> listPostAll(Integer pageNum, Integer pageSize, Integer userId) {
        PageHelper.startPage(pageNum,pageSize);
        List<Post> postList = postMapper.queryAllPost();
        PageInfo<Post> pageInfo = new PageInfo<>(postList);
        List<ResponsePostDto> responsePostDtos = getPostsDetail(postList, userId);
        PageInfo<ResponsePostDto> resultPageInfo = new PageInfo<>(responsePostDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());
        return resultPageInfo;
    }

    @Override
    public PageInfo<ResponsePostDto> listPostByFilter(PostFilterDto postFilterDto, Integer pageNum, Integer pageSize, Integer userId) {
        PageHelper.startPage(pageNum,pageSize);
        List<Post> postList = postMapper.queryPostByFilter(postFilterDto);
        PageInfo<Post> pageInfo = new PageInfo<>(postList);
        if (postList.isEmpty()) {
            return PageInfo.of(new ArrayList<>());
        }
        List<ResponsePostDto> responsePostDtos = getPostsDetail(postList, userId);
        PageInfo<ResponsePostDto> resultPageInfo = new PageInfo<>(responsePostDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());
        return resultPageInfo;
    }

    @Override
    public List<ResponsePostDto> getPostsDetail(List<Post> postList, Integer userId) {
        // 提取用户ID列表
        List<Integer> userIds = postList.stream()
                .map(Post::getUserId)
                .distinct()
                .toList();
        List<Integer> postIds = postList.stream()
                .map(Post::getPostId)
                .toList();
        // 批量查询评论数（一级 + 二级）
        List<PostCommentStatisticVO> commentCountList =
                postMapper.getPostsCommentCounts(postIds);

        // 转成 Map<postId, count>
        Map<Integer, Integer> postCommentCountMap = commentCountList.stream()
                .collect(Collectors.toMap(
                        PostCommentStatisticVO::getPostId,
                        PostCommentStatisticVO::getCount
                ));
        // 批量查询点赞数
        List<PostCommentStatisticVO> likeCountList = postMapper.getPostsLikeCounts(postIds);
        Map<Integer, Integer> postLikeCountMap = likeCountList.stream()
                .collect(Collectors.toMap(
                        PostCommentStatisticVO::getPostId,
                        PostCommentStatisticVO::getCount
                ));
        // 批量查询收藏数
        List<PostCommentStatisticVO> favoriteCountList = postMapper.getPostsFavoriteCounts(postIds);
        Map<Integer, Integer> postFavoriteCountMap = favoriteCountList.stream()
                .collect(Collectors.toMap(
                        PostCommentStatisticVO::getPostId,
                        PostCommentStatisticVO::getCount
                ));
        // 批量查询浏览数
        List<PostCommentStatisticVO> viewCountList = postMapper.getPostsViewCounts(postIds);
        Map<Integer, Integer> postViewCountMap = viewCountList.stream()
                .collect(Collectors.toMap(
                        PostCommentStatisticVO::getPostId,
                        PostCommentStatisticVO::getCount
                ));
        // 批量查询用户帖子点赞信息
        List<PostIdJudgeVO> isLikedList = postMapper.getPostIsLiked(postIds, userId);
        Map<Integer, Integer> isLikedMap = isLikedList.stream()
                .collect(Collectors.toMap(
                        PostIdJudgeVO::getPostId,
                        PostIdJudgeVO::getStatus
                ));
        // 批量查询用户帖子收藏信息
        List<PostIdJudgeVO> isFavoritedList = postMapper.getPostIsFavorite(postIds, userId);
        Map<Integer, Integer> isFavoritedMap = isFavoritedList.stream()
                .collect(Collectors.toMap(
                        PostIdJudgeVO::getPostId,
                        PostIdJudgeVO::getStatus
                ));
        Map<Integer, UserInfoVO> userInfoMap = commentService.getUserInfoMap(userIds);
        List<ResponsePostDto> responsePostDtos = new ArrayList<>();
        for (Post post : postList){
            ResponsePostDto responsePostDto = new ResponsePostDto();
            responsePostDto.setPostId(post.getPostId());
            responsePostDto.setUserId(post.getUserId());
            responsePostDto.setTitle(post.getTitle());
            responsePostDto.setContent(post.getContent());
            responsePostDto.setStatus(post.getStatus());
            responsePostDto.setCreateTime(post.getCreateTime());
            // 设置用户信息
            UserInfoVO userInfo = userInfoMap.get(post.getUserId());
            if (userInfo != null) {
                responsePostDto.setUsername(userInfo.getUsername());
                responsePostDto.setAvatar(serviceUserUrl + userInfo.getAvatar());
                responsePostDto.setUserSignature(userInfo.getUserSignature());
            }
            responsePostDto.setCommentNum(postCommentCountMap.getOrDefault(post.getPostId(), 0));
            responsePostDto.setLikeNum(postLikeCountMap.getOrDefault(post.getPostId(), 0));
            responsePostDto.setFavoriteNum(postFavoriteCountMap.getOrDefault(post.getPostId(), 0));
            responsePostDto.setViewNum(postViewCountMap.getOrDefault(post.getPostId(), 0));
            responsePostDto.setIsLiked(isLikedMap.getOrDefault(post.getPostId(), 0));
            responsePostDto.setIsFavorite(isFavoritedMap.getOrDefault(post.getPostId(), 0));
            List<String> imgUrls = postMapper.queryPostImgUrls(post.getPostId());
            List<String> imgUrlList = new ArrayList<>();
            for (String imgUrl : imgUrls){
                imgUrl = serviceCommunityUrl + imgUrl;
                imgUrlList.add(imgUrl);
            }
            List<TagVO> tags = postMapper.queryPostTags(post.getPostId());
            responsePostDto.setTags(tags);
            responsePostDto.setImgUrls(imgUrlList);
            responsePostDtos.add(responsePostDto);
        }
        return responsePostDtos;
    }

    @Override
    public List<TagVO> queryAllPostTags() {
        return postMapper.queryAllTags();
    }

    @Override
    public int modifyLike(Integer postId, Integer userId) {
        if (postMapper.isExistLike(postId, userId)){
            log.info("用户(user_id:{})取消点赞帖子(post_id:{})", userId, postId);
            return postMapper.deleteLike(postId, userId);
        }else {
            log.info("用户(user_id:{})点赞帖子(post_id:{})", userId, postId);
            return postMapper.addLike(postId, userId);
        }
    }

    @Override
    public int modifyFavorite(Integer postId, Integer userId) {
        if (postMapper.isExistFavorite(postId, userId)){
            log.info("用户(user_id:{})取消收藏帖子(post_id:{})", userId, postId);
            return postMapper.deleteFavorite(postId, userId);
        }else {
            log.info("用户(user_id:{})收藏帖子(post_id:{})", userId, postId);
            return postMapper.addFavorite(postId, userId);
        }
    }

    @Override
    public int addView(Integer postId, Integer userId, String ipAddress) {
        if (postMapper.checkRecentView(postId, userId)) {
            // 已经在1小时内浏览过，不重复计数
            log.info("用户(user_id:{})在1小时内已浏览过帖子(post_id:{}), 不重复计数", userId, postId);
            return 1;
        }
        log.info("用户(user_id:{})浏览帖子(post_id:{})", userId, postId);
        return postMapper.addView(postId, ipAddress, userId);
    }

    @Override
    public List<UserInfoVO> getUpdateUploaderInfo(Integer userId) {
        Set<Object> updates = redisTemplate.opsForSet()
                .members("follow:update:" + userId);
        if (updates != null){
            List<Integer> updateList = updates.stream()
                    .map(obj -> Integer.parseInt(obj.toString()))
                    .toList();
            List<UserInfoVO> updateUserInfoList = commentService.getUserInfoMap(updateList).values().stream()
                    .toList();
            for (UserInfoVO updateUserInfo : updateUserInfoList){
                updateUserInfo.setAvatar(serviceUserUrl + updateUserInfo.getAvatar());
            }
            return updateUserInfoList;
        }else {
            return null;
        }
    }

    @Override
    public void clearFollowUpdate(Integer userId, Integer uploaderId) {
//        log.info("用户(user_id:{})已读关注用户(uploader_id:{})的帖子", userId, uploaderId);
        redisTemplate.opsForSet().remove("follow:update:" + userId, uploaderId);
    }

    @Override
    public PageInfo<PostFavoriteInfoDto> listFavoritePost(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Post> postList = postMapper.queryPostsByFavorite(userId);
        PageInfo<Post> pageInfo = new PageInfo<>(postList);
        List<PostFavoriteInfoDto> postFavoriteInfoDtos = new ArrayList<>();
        for (Post post : postList){
            PostFavoriteInfoDto postFavoriteInfoDto = new PostFavoriteInfoDto();
            postFavoriteInfoDto.setPostId(post.getPostId());
            UserInfoVO userInfoVO = commentService.getUserInfoMap(List.of(post.getUserId())).get(post.getUserId());
            postFavoriteInfoDto.setPostUserId(post.getUserId());
            postFavoriteInfoDto.setPostUserAvatar(serviceUserUrl + userInfoVO.getAvatar());
            postFavoriteInfoDto.setTitle(post.getTitle());
            postFavoriteInfoDto.setTags(postMapper.queryPostTags(post.getPostId()));
            postFavoriteInfoDto.setCreateTime(post.getCreateTime());
            postFavoriteInfoDtos.add(postFavoriteInfoDto);
        }
        PageInfo<PostFavoriteInfoDto> resultPageInfo = new PageInfo<>(postFavoriteInfoDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());
        return resultPageInfo;
    }
}
