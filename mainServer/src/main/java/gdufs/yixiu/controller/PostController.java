package gdufs.yixiu.controller;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.community.request.PostFilterDto;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.response.ResponsePostDto;
import gdufs.yixiu.pojo.Post;
import gdufs.yixiu.service.ImgUploadService;
import gdufs.yixiu.service.PostService;
import gdufs.yixiu.util.IPUtils;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/community/post")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private ImgUploadService imgUploadService;
    @Autowired
    private JWTUtils jwtUtils;

    @UserLoginToken
    @GetMapping("/list")
    public Result getAllPosts(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                              HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(postService.listPostAll(pageNum, pageSize, userId));
    }
    @UserLoginToken
    @GetMapping("/listByFilter")
    public Result getAllPostsByFilter(PostFilterDto postFilterDto,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                      HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        if (postFilterDto.getPostId() != null){
            PageInfo<ResponsePostDto> result = new PageInfo<>();
            List<ResponsePostDto> responsePostDtos = new ArrayList<>();
            responsePostDtos.add(postService.getPostByPostId(postFilterDto.getPostId(), userId));
            result.setList(responsePostDtos);
            result.setTotal(1);
            return Result.success(result);
        }else {
            return Result.success(postService.listPostByFilter(postFilterDto, pageNum, pageSize, userId));
        }
    }
    @UserLoginToken
    @GetMapping("/allTags")
    public Result getAllTags(){
        return Result.success(postService.queryAllPostTags());
    }

    @UserLoginToken
    @PostMapping("/create")
    public Result createPost(@RequestBody RequestPostDto requestPostDto, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        requestPostDto.setUserId(jwtUtils.getInfoFromToken(token).getId());
        int postId = postService.createPost(requestPostDto);
        log.info("用户No.{} 上传帖子No.{}", requestPostDto.getUserId(), postId);
        Map<String, Integer> result = new HashMap<>();
        result.put("postId", postId);
        return Result.success(result);
    }
    @UserLoginToken
    @DeleteMapping("/delete")
    public Result deletePost(@RequestParam("postId") Integer postId,
                             HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        String role = jwtUtils.getInfoFromToken(token).getRole();
        int result = postService.deletePost(postId, userId, role);
        if (result == 200){
            return Result.success(null);
        }else if (result == 403){
            return Result.insufficientPermissions();
        }else {
            return Result.fail("未知问题");
        }
    }

    @UserLoginToken
    @PostMapping("/uploadPostImg")
    public Result uploadPostImg(@RequestParam("img") MultipartFile[] files,
                                @RequestParam("postId") Integer postId,
                                HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户No.{} 上传帖子No.{} 的图片", userId, postId);
        int count = 1;
        List<String> imgUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imgUrl = imgUploadService.uploadPostImg(file, postId, count);
            imgUrls.add(imgUrl);
            count++;
        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("logImgUrls", imgUrls);
        return Result.success(map);
    }
    @UserLoginToken
    @PostMapping("/modifyLike")
    public Result modifyLike(Integer postId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        int result = postService.modifyLike(postId, userId);
        return result == 1 ? Result.success(null) : Result.fail("修改失败");
    }
    @UserLoginToken
    @PostMapping("/modifyFavorite")
    public Result modifyFavorite(Integer postId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        int result = postService.modifyFavorite(postId, userId);
        return result == 1 ? Result.success(null) : Result.fail("修改失败");
    }
    @UserLoginToken
    @PostMapping("/addView")
    public Result addView(Integer postId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        String ip = IPUtils.getClientIP(request);
        int result = postService.addView(postId, userId, ip);
        return result == 1 ? Result.success(null) : Result.fail("添加失败");
    }
    @UserLoginToken
    @GetMapping("/getUpdateInfo")
    public Result getUpdateInfo(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(postService.getUpdateUploaderInfo(userId));
    }
    @UserLoginToken
    @PostMapping("/clearFollowUpdate")
    public Result clearFollowUpdate(Integer uploaderId,
                                    HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        postService.clearFollowUpdate(userId, uploaderId);
        return Result.success(null);
    }
    @UserLoginToken
    @GetMapping("/getFavoritePostInfo")
    public Result getFavoritePostInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(postService.listFavoritePost(userId, pageNum, pageSize));
    }
}
