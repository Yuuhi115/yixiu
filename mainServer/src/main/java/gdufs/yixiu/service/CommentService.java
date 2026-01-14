package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.dto.community.response.ResponseCommentDto;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.PostCommentReply;

import java.util.List;

public interface CommentService {

    int addComment(RequestCommentDto requestCommentDto);

    int addReply(RequestReplyDto requestReplyDto);
    PageInfo<ResponseCommentDto> listComments(Integer postId, Integer userId, Integer pageNum, Integer pageSize);
    PageInfo<PostCommentReply> listReplies(Integer commentId, Integer pageNum, Integer pageSize);
    int deleteComment(Integer commentId);
    int deleteReply(Integer replyId);
    int countCommentsNum(Integer postId);

    int modifyCommentLike(Integer commentId, Integer userId);
    int modifyReplyLike(Integer replyId, Integer userId);
    List<ResponseCommentDto> postCommentToResponseCommentDto(List<PostComment> postComments, Integer userId);
}
