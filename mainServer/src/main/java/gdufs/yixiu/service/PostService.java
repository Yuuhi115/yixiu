package gdufs.yixiu.service;



import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.response.ResponsePostDto;
import gdufs.yixiu.pojo.Post;


public interface PostService {
    int createPost(RequestPostDto requestPostDto);
    Post getPostByPostId(Integer postId);
    PageInfo<ResponsePostDto> listPostAll(Integer pageNum, Integer pageSize);
    ResponsePostDto getPostDetail(Post post);

    int modifyLike(Integer postId, Integer userId);
    int modifyFavorite(Integer postId, Integer userId);
    int addView(Integer postId, Integer userId, String ipAddress);
}
