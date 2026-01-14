package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.CommentMapper;
import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.dto.community.response.CommentStatisticCountVO;
import gdufs.yixiu.dto.community.response.ResponseCommentDto;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.PostCommentReply;
import gdufs.yixiu.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Override
    public int addComment(RequestCommentDto requestCommentDto) {
        return commentMapper.addComment(requestCommentDto);
    }

    @Override
    public int addReply(RequestReplyDto requestReplyDto) {
        return commentMapper.addReply(requestReplyDto);
    }

    @Override
    public List<ResponseCommentDto> postCommentToResponseCommentDto(List<PostComment> postComments, Integer userId) {
        // 提取评论ID列表
        List<Integer> commentIds = postComments.stream()
                .map(PostComment::getCommentId)
                .toList();
//        log.info("批量查询评论列表中点赞数和回复数，评论ID列表：{}", commentIds);
        List<Integer> likedIds = commentMapper.selectLikedCommentIds(userId, commentIds);

        Set<Integer> likedSet = new HashSet<>(likedIds);
//        log.info("用户(user_id:{})点赞的评论ID列表：{}", userId, likedIds);

        // 批量查询评论列表中点赞数和回复数
        List<CommentStatisticCountVO> likeCounts = commentMapper.getCommentLikeCounts(commentIds);
        List<CommentStatisticCountVO> replyCounts = commentMapper.getCommentReplyCounts(commentIds);
//        log.info("批量查询评论列表中点赞数结果：{}", likeCounts);
//        log.info("批量查询评论列表中回复数结果：{}", replyCounts);

        // 创建点赞数和回复数映射
        Map<Integer, Integer> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        CommentStatisticCountVO::getCommentId,
                        CommentStatisticCountVO::getCount
                ));
        Map<Integer, Integer> replyCountMap = replyCounts.stream()
                .collect(Collectors.toMap(
                        CommentStatisticCountVO::getCommentId,
                        CommentStatisticCountVO::getCount
                ));

        List<ResponseCommentDto> responseCommentDtos = new ArrayList<>();

        // 设置每个评论的点赞数和回复数
        for (PostComment comment : postComments) {
            ResponseCommentDto responseCommentDto = new ResponseCommentDto();
            responseCommentDto.setCommentId(comment.getCommentId());
            responseCommentDto.setPostId(comment.getPostId());
            responseCommentDto.setUserId(comment.getUserId());
            responseCommentDto.setContent(comment.getContent());
            responseCommentDto.setLikeNum(likeCountMap.getOrDefault(comment.getCommentId(), 0));
            responseCommentDto.setReplyNum(replyCountMap.getOrDefault(comment.getCommentId(), 0));
            responseCommentDto.setCreateTime(comment.getCreateTime());
            responseCommentDto.setIsLike(likedSet.contains(comment.getCommentId()) ? 1 : 0);
            responseCommentDtos.add(responseCommentDto);
        }
        return responseCommentDtos;
    }

    @Override
    public PageInfo<ResponseCommentDto> listComments(Integer postId, Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PostComment> postComments = commentMapper.listComments(postId);
        PageInfo<PostComment> originalCommentList = new PageInfo<>(postComments);
        List<ResponseCommentDto> responseCommentDtos = postCommentToResponseCommentDto(originalCommentList.getList(), userId);
        PageInfo<ResponseCommentDto> resultPageInfo = new PageInfo<>(responseCommentDtos);
        resultPageInfo.setTotal(originalCommentList.getTotal());
        resultPageInfo.setPages(originalCommentList.getPages());
        resultPageInfo.setPageNum(originalCommentList.getPageNum());
        resultPageInfo.setPageSize(originalCommentList.getPageSize());
        return resultPageInfo;
    }

    @Override
    public PageInfo<PostCommentReply> listReplies(Integer commentId, Integer pageNum, Integer pageSize) {
        return null;
    }

    @Override
    public int deleteComment(Integer commentId) {
        return 0;
    }

    @Override
    public int deleteReply(Integer replyId) {
        return 0;
    }

    @Override
    public int countCommentsNum(Integer postId) {
        return 0;
    }

    @Override
    public int modifyCommentLike(Integer commentId, Integer userId) {
        if (commentMapper.isExistCommentLiked(commentId, userId)){
            log.info("用户(user_id:{})取消点赞评论(comment_id:{})", userId, commentId);
            return commentMapper.deleteCommentLike(commentId, userId);
        }else {
            log.info("用户(user_id:{})点赞评论(comment_id:{})", userId, commentId);
            return commentMapper.addCommentLike(commentId, userId);
        }
    }

    @Override
    public int modifyReplyLike(Integer replyId, Integer userId) {
        if (commentMapper.isExistReplyLiked(replyId, userId)){
            log.info("用户(user_id:{})取消点赞回复(reply_id:{})", userId, replyId);
            return commentMapper.deleteReplyLike(replyId, userId);
        } else {
            log.info("用户(user_id:{})点赞回复(reply_id:{})", userId, replyId);
            return commentMapper.addReplyLike(replyId, userId);
        }
    }


}
