package gdufs.yixiu.dao;

import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.dto.community.vo.CommentStatisticCountVO;
import gdufs.yixiu.dto.community.vo.PostCommentStatisticVO;
import gdufs.yixiu.dto.community.vo.ReplyStatisticCountVO;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.PostCommentReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    int addComment(RequestCommentDto requestCommentDto);
    int addReply(RequestReplyDto requestReplyDto);
    PostComment getCommentByCommentId(Integer commentId);
    PostCommentReply getReplyByReplyId(Integer replyId);
    List<PostComment> listComments(Integer postId);
    List<PostCommentReply> listReplies(Integer commentId);
    int updateComment(PostComment postComment);
    int updateReply(PostCommentReply postCommentReply);
    int deleteComment(Integer commentId);
    int deleteReply(Integer replyId);
    int countCommentsByPostId(Integer postId);

    int addCommentLike(@Param("commentId") Integer commentId,
                        @Param("userId") Integer userId);
    int deleteCommentLike(@Param("commentId") Integer commentId,
                           @Param("userId") Integer userId);
    Boolean isExistCommentLiked(@Param("commentId") Integer commentId,
                           @Param("userId") Integer userId);

    int addReplyLike(@Param("replyId") Integer replyId,
                      @Param("userId") Integer userId);

    int deleteReplyLike(@Param("replyId") Integer replyId,
                        @Param("userId") Integer userId);
    Boolean isExistReplyLiked(@Param("replyId") Integer replyId,
                           @Param("userId") Integer userId);
    // 批量查询评论列表中是否被当前用户点赞
    List<Integer> selectLikedCommentIds(
            @Param("userId") Integer userId,
            @Param("commentIds") List<Integer> commentIds
    );
    // 批量查询评论列表中点赞数和回复数
    List<CommentStatisticCountVO> getCommentLikeCounts(@Param("commentIds") List<Integer> commentIds);
    List<CommentStatisticCountVO> getCommentReplyCounts(@Param("commentIds") List<Integer> commentIds);

    /** 默认加载前 20 条（二级评论批量） */
    List<PostCommentReply> getRepliesByCommentIds(
            @Param("commentIds") List<Integer> commentIds,
            @Param("limit") Integer limit
    );

    /** 展开更多（二级评论分页） */
    List<PostCommentReply> getReplyByCommentId(@Param("commentId") Integer commentId);

    List<ReplyStatisticCountVO> getReplyLikeCounts(@Param("replyIds") List<Integer> replyIds);

    List<Integer> selectLikedReplyIds(
            @Param("userId") Integer userId,
            @Param("replyIds") List<Integer> replyIds
    );
}
