package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.CommentMapper;
import gdufs.yixiu.dao.PostMapper;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.response.ResponsePostDto;
import gdufs.yixiu.pojo.Post;
import gdufs.yixiu.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;
    private String serviceCommunityUrl;
    @Value("${resources-path.service-community-url}")
    public void setServiceCommunityUrl(String serviceCommunityUrl) {
        this.serviceCommunityUrl = serviceCommunityUrl;
    }
    @Override
    public int createPost(RequestPostDto requestPostDto) {
        Post post = new Post();
        post.setUserId(requestPostDto.getUserId());
        post.setTitle(requestPostDto.getTitle());
        post.setContent(requestPostDto.getContent());
        int row = postMapper.addPost(post);
        log.info("用户(id:{})上传帖子成功，帖子id为：{}",post.getUserId(), post.getPostId());
        return row == 1 ? post.getPostId() : 0;
    }

    @Override
    public Post getPostByPostId(Integer postId) {
        return null;
    }

    @Override
    public PageInfo<ResponsePostDto> listPostAll(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Post> postList = postMapper.queryAllPost();
        PageInfo<Post> pageInfo = new PageInfo<>(postList);
        List<ResponsePostDto> responsePostDtos = new ArrayList<>();
        for (Post post : postList) {
            responsePostDtos.add(getPostDetail(post));
        }
        PageInfo<ResponsePostDto> resultPageInfo = new PageInfo<>(responsePostDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());
        return resultPageInfo;
    }

    @Override
    public ResponsePostDto getPostDetail(Post post) {
        ResponsePostDto responsePostDto = new ResponsePostDto();
        responsePostDto.setPostId(post.getPostId());
        responsePostDto.setUserId(post.getUserId());
        responsePostDto.setTitle(post.getTitle());
        responsePostDto.setContent(post.getContent());
        responsePostDto.setStatus(post.getStatus());
        responsePostDto.setCommentNum(commentMapper.countCommentsByPostId(post.getPostId()));
        Map<String, Object> likeFavViewNum = postMapper.countPostLikeFavViewNum(post.getPostId());
        responsePostDto.setLikeNum(Integer.valueOf(likeFavViewNum.get("likeNum").toString()));
        responsePostDto.setFavoriteNum(Integer.valueOf(likeFavViewNum.get("favoriteNum").toString()));
        responsePostDto.setViewNum(Integer.valueOf(likeFavViewNum.get("viewNum").toString()));
        responsePostDto.setCreateTime(post.getCreateTime());
        List<String> imgUrls = postMapper.queryPostImgUrls(post.getPostId());
        List<String> imgUrlList = new ArrayList<>();
        for (String imgUrl : imgUrls){
            imgUrl = serviceCommunityUrl + imgUrl;
            imgUrlList.add(imgUrl);
        }
        responsePostDto.setImgUrls(imgUrlList);
        return responsePostDto;
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
}
