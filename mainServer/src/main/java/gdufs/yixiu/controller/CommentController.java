package gdufs.yixiu.controller;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.UserCommentListDto;
import gdufs.yixiu.dto.community.request.CommentReplyDto;
import gdufs.yixiu.dto.community.request.RequestCommentDto;
import gdufs.yixiu.dto.community.request.RequestReplyDto;
import gdufs.yixiu.dto.filter.UserCommentListFilter;
import gdufs.yixiu.pojo.PostComment;
import gdufs.yixiu.service.CommentService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (result == -1){
            return Result.fail("添加失败");
        }else {
            Map<String, Integer> map = new HashMap<>();
            map.put("commentId", result);
            return Result.success(map);
        }
    }
    @UserLoginToken
    @GetMapping("/listByPostId")
    public Result commentListByPostId(@RequestParam("postId") Integer postId,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                      @RequestParam(value = "commentId", defaultValue = "0") Integer commentId,
                                      HttpServletRequest request){
        // commentId不为0时，表示优先获取该评论
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(commentService.listComments(postId, commentId, userId, pageNum, pageSize));
    }
    @UserLoginToken
    @GetMapping("/replyListByCommentId")
    public Result repliesPageByCommentId(@RequestParam("commentId") Integer commentId,
                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                         @RequestParam(value = "replyId", defaultValue = "0") Integer replyId,
                                         HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(commentService.listReplies(commentId, replyId, userId, pageNum, pageSize));
    }
    @UserLoginToken
    @PostMapping("/addReply")
    public Result addReply(@RequestBody RequestReplyDto requestReplyDto,
                           HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        requestReplyDto.setFromUserId(userId);
        int result = commentService.addReply(requestReplyDto);
        if (result == -1){
            return Result.fail("添加失败");
        }else {
            Map<String, Integer> map = new HashMap<>();
            map.put("replyId", result);
            return Result.success(map);
        }
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
            Integer deleteReplyCount = commentService.deleteRepliesByCommentId(commentReplyDto.getCommentId());
            log.info("用户(user_id:{})删除该评论下的{}条回复", userId, deleteReplyCount);
        }else {
            if (!commentService.isReplyOwner(commentReplyDto.getReplyId(), userId) && !role.equals("admin") && !role.equals("super_admin")){
                return Result.fail("操作无权限");
            }
            log.info("用户(user_id:{})删除回复(reply_id:{})", userId, commentReplyDto.getReplyId());
            result = commentService.deleteReply(commentReplyDto.getReplyId());
        }
        return result == 1 ? Result.success(null) : Result.fail("删除失败");
    }
    @AdminLoginToken
    @GetMapping("/getByUserId")
    public Result getCommentByUserId(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                     @Validated UserCommentListFilter userCommentListFilter
                                     ){
        return Result.success(commentService.getUserCommentList(userCommentListFilter, pageNum, pageSize));
    }
    @AdminLoginToken
    @GetMapping("/getReplyByUserId")
    public Result getReplyByUserId(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                  @Validated UserCommentListFilter userCommentListFilter
                                  ){
        return Result.success(commentService.getUserReplyList(userCommentListFilter, pageNum, pageSize));
    }
}
