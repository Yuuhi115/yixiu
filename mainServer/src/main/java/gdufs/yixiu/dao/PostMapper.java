package gdufs.yixiu.dao;

import gdufs.yixiu.dto.community.request.PostFilterDto;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.pojo.Post;
import gdufs.yixiu.pojo.PostImg;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper {
    int addPost(Post post);
    int deletePost(Integer postId);
    int addPostImg(PostImg postImg);
    List<Post> queryAllPost();
    Post queryPostById(Integer postId);
    List<Post> queryPostByUserId(Integer userId);
    List<Post> queryPostByFilter(PostFilterDto filterDto);
    int updatePost(RequestPostDto requestPostDto);
    Map<String, Object> countPostLikeFavViewNum(Integer postId);
    List<String> queryPostImgUrls(Integer postId);

    int addLike(@Param("postId") Integer postId,
                @Param("userId") Integer userId);
    int addFavorite(@Param("postId") Integer postId,
                    @Param("userId") Integer userId);
    int addView(@Param("postId") Integer postId,
                 @Param("ipAddress") String ipAddress,
                 @Param("userId") Integer userId);
    Boolean checkRecentView(@Param("postId") Integer postId,
                            @Param("userId") Integer userId);
    int deleteLike(@Param("postId") Integer postId,
                   @Param("userId") Integer userId);
    int deleteFavorite(@Param("postId") Integer postId,
                       @Param("userId") Integer userId);
    Boolean isExistLike(@Param("postId") Integer postId,
                   @Param("userId") Integer userId);
    Boolean isExistFavorite(@Param("postId") Integer postId,
                        @Param("userId") Integer userId);
}
