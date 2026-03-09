package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.UserCommentListDto;
import gdufs.yixiu.dto.UserReplyListDto;
import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.dto.community.response.ResponseCommentDto;
import gdufs.yixiu.dto.community.response.ResponseReplyDto;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.dto.filter.UserCommentListFilter;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.PostCommentReply;

import java.util.List;
import java.util.Map;

public interface CommentService {

    int addComment(RequestCommentDto requestCommentDto);

    int addReply(RequestReplyDto requestReplyDto);
    PageInfo<ResponseCommentDto> listComments(Integer postId, Integer commentId, Integer userId, Integer pageNum, Integer pageSize);
    PageInfo<ResponseReplyDto> listReplies(Integer commentId, Integer replyId, Integer userId, Integer pageNum, Integer pageSize);
    int deleteComment(Integer commentId);
    int deleteReply(Integer replyId);
    int countCommentsNum(Integer postId);
    PostComment getCommentByCommentId(Integer commentId);
    PostCommentReply getReplyByReplyId(Integer replyId);
    Boolean isCommentOwner(Integer commentId, Integer userId);
    Boolean isReplyOwner(Integer replyId, Integer userId);

    int modifyCommentLike(Integer commentId, Integer userId);
    int modifyReplyLike(Integer replyId, Integer userId);
    List<ResponseCommentDto> postCommentToResponseCommentDto(List<PostComment> postComments, Integer userId);
    Map<Integer, List<ResponseReplyDto>> buildReplyGroup(List<PostCommentReply> replies, Integer userId);
    Map<Integer, UserInfoVO> getUserInfoMap(List<Integer> userIds);
    PageInfo<UserCommentListDto> getUserCommentList(UserCommentListFilter userCommentListFilter, Integer pageNum, Integer pageSize);
    PageInfo<UserReplyListDto> getUserReplyList(UserCommentListFilter userCommentListFilter, Integer pageNum, Integer pageSize);
}
