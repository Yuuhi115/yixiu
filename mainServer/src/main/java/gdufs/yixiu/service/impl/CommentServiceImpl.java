package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.CommentMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.UserCommentListDto;
import gdufs.yixiu.dto.UserReplyListDto;
import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.dto.community.vo.CommentStatisticCountVO;
import gdufs.yixiu.dto.community.vo.ReplyStatisticCountVO;
import gdufs.yixiu.dto.community.response.ResponseCommentDto;
import gdufs.yixiu.dto.community.response.ResponseReplyDto;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.dto.filter.UserCommentListFilter;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.pojo.PostCommentReply;
import gdufs.yixiu.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UsersMapper usersMapper;
    private String serviceUserUrl;
    @Value("${resources-path.service-avatar-url}")
    public void setServiceUserUrl(String serviceUserUrl) {
        this.serviceUserUrl = serviceUserUrl;
    }
    @Override
    public int addComment(RequestCommentDto requestCommentDto) {
        int row = commentMapper.addComment(requestCommentDto);
        Integer commentId = requestCommentDto.getCommentId();
        return row > 0 ? commentId : -1;
    }

    @Override
    public int addReply(RequestReplyDto requestReplyDto) {
        int row = commentMapper.addReply(requestReplyDto);
        Integer replyId = requestReplyDto.getReplyId();
        return row > 0 ? replyId : -1;
    }

    @Override
    public List<ResponseCommentDto> postCommentToResponseCommentDto(List<PostComment> postComments, Integer userId) {
        // 提取用户ID列表
        List<Integer> userIds = postComments.stream()
                .map(PostComment::getUserId)
                .distinct() // 去重，避免重复查询
                .toList();
        // 批量查询用户信息
        Map<Integer, UserInfoVO> userInfoMap = getUserInfoMap(userIds);
        // 提取评论ID列表
        List<Integer> commentIds = postComments.stream()
                .map(PostComment::getCommentId)
                .toList();
        if (commentIds.isEmpty()) {
            return List.of();
        }
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
/* 防止数据库压力过大，2026.1.24废除以下注释代码 */
        // 默认加载前 20 条二级评论
//        List<PostCommentReply> replies = commentMapper.getRepliesByCommentIds(commentIds, 20);

        // 组装二级评论
//        Map<Integer, List<ResponseReplyDto>> replyGroupMap = buildReplyGroup(replies, userId);

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
            // 设置用户信息
            UserInfoVO userInfo = userInfoMap.get(comment.getUserId());
            if (userInfo != null) {
                responseCommentDto.setUsername(userInfo.getUsername());
                responseCommentDto.setAvatar(serviceUserUrl + userInfo.getAvatar());
            }
//            responseCommentDto.setReplyList(replyGroupMap.getOrDefault(comment.getCommentId(), List.of()));
            responseCommentDtos.add(responseCommentDto);
        }
        return responseCommentDtos;
    }

    @Override
    public Map<Integer, List<ResponseReplyDto>> buildReplyGroup(List<PostCommentReply> replies, Integer userId) {
        if (replies == null || replies.isEmpty()) {
            return Map.of();
        }
        List<Integer> fromUserIds = replies.stream()
                .map(PostCommentReply::getFromUserId)
                .distinct()
                .toList();
        List<Integer> toUserIds = replies.stream()
                .map(PostCommentReply::getToUserId)
                .distinct()
                .toList();
        Map<Integer, UserInfoVO> fromUserInfoMap = getUserInfoMap(fromUserIds);
        Map<Integer, UserInfoVO> toUserInfoMap = getUserInfoMap(toUserIds);

        // 提取 replyIds
        List<Integer> replyIds = replies.stream()
                .map(PostCommentReply::getReplyId)
                .toList();

        // 二级评论点赞数（VO → Map）
        Map<Integer, Integer> likeCountMap =
                commentMapper.getReplyLikeCounts(replyIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ReplyStatisticCountVO::getReplyId,
                                ReplyStatisticCountVO::getCount
                        ));

        // 当前用户点赞的 replyId 集合
        Set<Integer> likedReplySet =
                new HashSet<>(commentMapper.selectLikedReplyIds(userId, replyIds));

        // 按 commentId 分组组装 ResponseReplyDto
        Map<Integer, List<ResponseReplyDto>> groupMap = new HashMap<>();

        for (PostCommentReply reply : replies) {
            ResponseReplyDto dto = new ResponseReplyDto();
            dto.setReplyId(reply.getReplyId());
            dto.setCommentId(reply.getCommentId());
            dto.setFromUserId(reply.getFromUserId());
            dto.setToUserId(reply.getToUserId());
            dto.setParentReplyId(reply.getParentReplyId());
            dto.setContent(reply.getContent());
            dto.setCreateTime(reply.getCreateTime());
            UserInfoVO fromUserInfo = fromUserInfoMap.get(reply.getFromUserId());
            if (fromUserInfo != null) {
                dto.setFromUserName(fromUserInfo.getUsername());
                dto.setFromUserAvatar(serviceUserUrl + fromUserInfo.getAvatar());
            }
            UserInfoVO toUserInfo = toUserInfoMap.get(reply.getToUserId());
            if (toUserInfo != null) {
                dto.setToUserName(toUserInfo.getUsername());
            }
            dto.setLikeNum(
                    likeCountMap.getOrDefault(reply.getReplyId(), 0)
            );
            dto.setIsLike(
                    likedReplySet.contains(reply.getReplyId()) ? 1 : 0
            );
            // 添加到分组
            groupMap
                    .computeIfAbsent(reply.getCommentId(), k -> new ArrayList<>())
                    .add(dto);
        }

        return groupMap;
    }

    @Override
    public Map<Integer, UserInfoVO> getUserInfoMap(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        // 调用 UserMapper 的批量查询方法
        List<UserInfoVO> userInfos = usersMapper.findUserNameAndAvatarByIds(userIds);

        return userInfos.stream()
                .collect(Collectors.toMap(
                        UserInfoVO::getUserId,
                        Function.identity()
                ));
    }

    @Override
    public PageInfo<UserCommentListDto> getUserCommentList(UserCommentListFilter userCommentListFilter, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserCommentListDto> userCommentList = commentMapper.findUserCommentListByFilter(userCommentListFilter);
        return new PageInfo<>(userCommentList);
    }

    @Override
    public PageInfo<UserReplyListDto> getUserReplyList(UserCommentListFilter userCommentListFilter, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserReplyListDto> userCommentList = commentMapper.findUserReplyListByFilter(userCommentListFilter);
        return new PageInfo<>(userCommentList);
    }


    @Override
    public PageInfo<ResponseCommentDto> listComments(Integer postId, Integer commentId, Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PostComment> postComments = commentMapper.listComments(postId);
        PageInfo<PostComment> originalCommentList = new PageInfo<>(postComments);
        if (commentId != 0){
            PostComment priorComment = commentMapper.getCommentByCommentId(commentId);
            if (priorComment != null) {
                // 检查 postComments 中是否已存在 priorComment
                boolean exists = postComments.stream()
                        .anyMatch(comment -> comment.getCommentId().equals(priorComment.getCommentId()));
                if (exists) {
                    // 如果存在，将其移动到第一位
                    postComments.removeIf(comment -> comment.getCommentId().equals(priorComment.getCommentId()));
                    postComments.addFirst(priorComment);
                } else {
                    // 如果不存在，插入到第一位并删除最后一条评论
                    postComments.addFirst(priorComment);
                    if (postComments.size() > pageSize) {
                        postComments.removeLast();
                    }
                }
            }
        }
        List<ResponseCommentDto> responseCommentDtos = postCommentToResponseCommentDto(postComments, userId);
        PageInfo<ResponseCommentDto> resultPageInfo = new PageInfo<>(responseCommentDtos);
        resultPageInfo.setTotal(originalCommentList.getTotal());
        resultPageInfo.setPages(originalCommentList.getPages());
        resultPageInfo.setPageNum(originalCommentList.getPageNum());
        resultPageInfo.setPageSize(originalCommentList.getPageSize());
        return resultPageInfo;
    }

    @Override
    public PageInfo<ResponseReplyDto> listReplies(Integer commentId, Integer replyId, Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<PostCommentReply> replies =
                commentMapper.getReplyByCommentId(commentId);
        PageInfo<PostCommentReply> originalReplyList = new PageInfo<>(replies);

        if (replies.isEmpty()) {
            return new PageInfo<>(List.of());
        }

        // 处理 priorReply 逻辑
        if (replyId != 0) {
            PostCommentReply priorReply = commentMapper.getReplyByReplyId(replyId);
            if (priorReply != null) {
                // 检查 replies 中是否已存在 priorReply
                boolean exists = replies.stream()
                        .anyMatch(reply -> reply.getReplyId().equals(priorReply.getReplyId()));
                if (exists) {
                    // 如果存在，将其移动到第一位
                    replies.removeIf(reply -> reply.getReplyId().equals(priorReply.getReplyId()));
                    replies.addFirst(priorReply);
                } else {
                    // 如果不存在，插入到第一位并删除最后一条回复
                    replies.addFirst(priorReply);
                    if (replies.size() > pageSize) {
                        replies.removeLast();
                    }
                }
            }
        }

        Map<Integer, List<ResponseReplyDto>> group =
                buildReplyGroup(replies, userId);
        PageInfo<ResponseReplyDto> resultPageInfo = new PageInfo<>(group.get(commentId));
        resultPageInfo.setTotal(originalReplyList.getTotal());
        resultPageInfo.setPages(originalReplyList.getPages());
        resultPageInfo.setPageNum(originalReplyList.getPageNum());
        resultPageInfo.setPageSize(originalReplyList.getPageSize());

       return resultPageInfo;
    }

    @Override
    public int deleteComment(Integer commentId) {
        PostComment postComment = new PostComment();
        postComment.setCommentId(commentId);
        postComment.setStatus(1);
        return commentMapper.updateComment(postComment) == 1 ? 1 : 0;
    }

    @Override
    public int deleteReply(Integer replyId) {
        PostCommentReply postCommentReply = new PostCommentReply();
        postCommentReply.setReplyId(replyId);
        postCommentReply.setStatus(1);
        return commentMapper.updateReply(postCommentReply) == 1 ? 1 : 0;
    }

    @Override
    public int countCommentsNum(Integer postId) {
        return 0;
    }

    @Override
    public PostComment getCommentByCommentId(Integer commentId) {
        return commentMapper.getCommentByCommentId(commentId);
    }

    @Override
    public PostCommentReply getReplyByReplyId(Integer replyId) {
        return commentMapper.getReplyByReplyId(replyId);
    }

    @Override
    public Boolean isCommentOwner(Integer commentId, Integer userId) {
        PostComment comment = commentMapper.getCommentByCommentId(commentId);
        return comment.getUserId().equals(userId);
    }

    @Override
    public Boolean isReplyOwner(Integer replyId, Integer userId) {
        PostCommentReply reply = commentMapper.getReplyByReplyId(replyId);
        return reply.getFromUserId().equals(userId);
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
