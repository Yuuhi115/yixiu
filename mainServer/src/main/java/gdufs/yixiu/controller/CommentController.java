package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.community.request.CommentReplyDto;
import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.service.CommentService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/community/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private JWTUtils jwtUtils;
    @UserLoginToken
    @PostMapping("/add")
    public Result addComment(@RequestBody RequestCommentDto requestCommentDto,
                             HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        requestCommentDto.setUserId(userId);
        int result = commentService.addComment(requestCommentDto);
        return result == 1 ? Result.success(null) : Result.fail("添加失败");
    }
    @UserLoginToken
    @GetMapping("/listByPostId")
    public Result commentListByPostId(@RequestParam("postId") Integer postId,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                      HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(commentService.listComments(postId, userId, pageNum, pageSize));
    }
    @UserLoginToken
    @GetMapping("/repliesPageByCommentId")
    public Result repliesPageByCommentId(@RequestParam("commentId") Integer commentId,
                                         @RequestParam(value = "pageNum", defaultValue = "2") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                         HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(commentService.listReplies(commentId, userId, pageNum, pageSize));
    }
    @UserLoginToken
    @PostMapping("/addReply")
    public Result addReply(@RequestBody RequestReplyDto requestReplyDto,
                           HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        requestReplyDto.setFromUserId(userId);
        int result = commentService.addReply(requestReplyDto);
        return result == 1 ? Result.success(null) : Result.fail("添加失败");
    }
    @UserLoginToken
    @PostMapping("/modifyCommentLike")
    public Result addCommentLike(CommentReplyDto commentReplyDto,
                                 HttpServletRequest request){
        if ((commentReplyDto.getCommentId() == null && commentReplyDto.getReplyId() == null) ||
                (commentReplyDto.getCommentId() != null && commentReplyDto.getReplyId() != null)) {
            return Result.fail("参数错误");
        }
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        int result;
        if (commentReplyDto.getCommentId() != null){
            result = commentService.modifyCommentLike(commentReplyDto.getCommentId(), userId);
        }else {
            result = commentService.modifyReplyLike(commentReplyDto.getReplyId(), userId);
        }
        return result == 1 ? Result.success(null) : Result.fail("修改失败");
    }
    @UserLoginToken
    @PutMapping("/delete")
    public Result deleteComment(CommentReplyDto commentReplyDto,
                                HttpServletRequest request){
        if ((commentReplyDto.getCommentId() == null && commentReplyDto.getReplyId() == null) ||
                (commentReplyDto.getCommentId() != null && commentReplyDto.getReplyId() != null)) {
            return Result.fail("参数错误");
        }
        String token = request.getHeader("Authorization");
        String role = jwtUtils.getInfoFromToken(token).getRole();
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        int result;
        if (commentReplyDto.getCommentId() != null){
            if (!commentService.isCommentOwner(commentReplyDto.getCommentId(), userId) && !role.equals("admin") && !role.equals("super_admin")){
                return Result.fail("操作无权限");
            }
            log.info("用户(user_id:{})删除评论(comment_id:{})", userId, commentReplyDto.getCommentId());
            result = commentService.deleteComment(commentReplyDto.getCommentId());
        }else {
            if (!commentService.isReplyOwner(commentReplyDto.getReplyId(), userId) && !role.equals("admin") && !role.equals("super_admin")){
                return Result.fail("操作无权限");
            }
            log.info("用户(user_id:{})删除回复(reply_id:{})", userId, commentReplyDto.getReplyId());
            result = commentService.deleteReply(commentReplyDto.getReplyId());
        }
        return result == 1 ? Result.success(null) : Result.fail("删除失败");
    }
}
