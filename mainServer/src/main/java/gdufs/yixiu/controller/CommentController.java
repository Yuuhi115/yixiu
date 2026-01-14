package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.UserLoginToken;
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
@RequestMapping("/api/v1/comment")
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
        return Result.success(commentService.listComments(postId, userId, pageNum, pageSize).getList());
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
    public Result addCommentLike(@RequestParam(name = "commentId", defaultValue = "0") Integer commentId,
                                 @RequestParam(name = "replyId", defaultValue = "0") Integer replyId,
                                 HttpServletRequest request){
        if ((commentId == 0 && replyId == 0) || (commentId != 0 && replyId != 0)) {
            return Result.fail("参数错误");
        }
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        int result;
        if (commentId != 0){
            result = commentService.modifyCommentLike(commentId, userId);
        }else {
            result = commentService.modifyReplyLike(replyId, userId);
        }
        return result == 1 ? Result.success(null) : Result.fail("修改失败");
    }
}
