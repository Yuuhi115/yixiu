package gdufs.yixiu.dao;

import gdufs.yixiu.dto.community.request.PostFilterDto;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.vo.LikeListIdsVO;
import gdufs.yixiu.dto.community.vo.PostCommentStatisticVO;
import gdufs.yixiu.dto.community.vo.PostIdJudgeVO;
import gdufs.yixiu.dto.community.vo.TagVO;
import gdufs.yixiu.pojo.Post;
import gdufs.yixiu.pojo.PostImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper {
    int addPost(Post post);
    int addPostTags(Integer postId, List<Integer> tags);
    int deletePost(Integer postId);
    int addPostImg(PostImg postImg);
    List<Post> queryAllPost();
    Post queryPostById(Integer postId);
    List<Post> queryPostByUserId(Integer userId);
    List<Post> queryPostByFilter(PostFilterDto filterDto);
    List<TagVO> queryPostTags(Integer postId);
    List<TagVO> queryAllTagsFreq();
    List<TagVO> queryAllTags();
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
    List<PostCommentStatisticVO> getPostsLikeCounts(List<Integer> postIds);
    List<PostCommentStatisticVO> getPostsFavoriteCounts(List<Integer> postIds);
    List<PostCommentStatisticVO> getPostsViewCounts(List<Integer> postIds);
    List<PostCommentStatisticVO> getPostsCommentCounts(List<Integer> postIds);
    List<PostIdJudgeVO> getPostIsLiked(@Param("postIds") List<Integer> postIds,
                                       @Param("userId") Integer userId);
    List<PostIdJudgeVO> getPostIsFavorite(@Param("postIds") List<Integer> postIds,
                                       @Param("userId") Integer userId);
    List<Integer> getPostIdsByUserId(Integer userId);
    List<Integer> getCommentIdsByUserId(Integer userId);
    List<Integer> getReplyIdsByUserId(Integer userId);
    Integer getPostsAndCommentsLikeCount(LikeListIdsVO likeListIdsVO);
    List<Post> queryPostsByFavorite(Integer userId);
}
