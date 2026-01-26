package gdufs.yixiu.service;



import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.community.request.PostFilterDto;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.response.ResponsePostDto;
import gdufs.yixiu.dto.community.vo.TagVO;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.pojo.Post;

import java.util.List;
import java.util.Map;


public interface PostService {
    int createPost(RequestPostDto requestPostDto);
    int deletePost(Integer postId, Integer userId, String role);
    ResponsePostDto getPostByPostId(Integer postId, Integer userId);
    PageInfo<ResponsePostDto> listPostAll(Integer pageNum, Integer pageSize, Integer userId);
    PageInfo<ResponsePostDto> listPostByFilter(PostFilterDto postFilterDto, Integer pageNum, Integer pageSize, Integer userId);
    List<ResponsePostDto> getPostsDetail(List<Post> postList, Integer userId);
    List<TagVO> queryAllPostTags();
    int modifyLike(Integer postId, Integer userId);
    int modifyFavorite(Integer postId, Integer userId);
    int addView(Integer postId, Integer userId, String ipAddress);
    List<UserInfoVO> getUpdateUploaderInfo(Integer userId);
    void clearFollowUpdate(Integer userId, Integer uploaderId);
}
