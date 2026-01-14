package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.community.request.RequestPostDto;
import gdufs.yixiu.dto.community.response.ResponsePostDto;
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
@RequestMapping("/api/v1/post")
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
                              @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize){
        return Result.success(postService.listPostAll(pageNum, pageSize).getList());
    }

    @UserLoginToken
    @PostMapping("/create")
    public Result createPost(@RequestBody RequestPostDto requestPostDto, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        requestPostDto.setUserId(jwtUtils.getInfoFromToken(token).getId());
        int postId = postService.createPost(requestPostDto);
        Map<String, Integer> result = new HashMap<>();
        result.put("postId", postId);
        return Result.success(result);
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
    public Result modifyLike(@RequestParam("postId") Integer postId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        int result = postService.modifyLike(postId, userId);
        return result == 1 ? Result.success(null) : Result.fail("修改失败");
    }
    @UserLoginToken
    @PostMapping("/modifyFavorite")
    public Result modifyFavorite(@RequestParam("postId") Integer postId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        int result = postService.modifyFavorite(postId, userId);
        return result == 1 ? Result.success(null) : Result.fail("修改失败");
    }
    @UserLoginToken
    @PostMapping("/addView")
    public Result addView(@RequestParam("postId") Integer postId, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        int userId = jwtUtils.getInfoFromToken(token).getId();
        String ip = IPUtils.getClientIP(request);
        int result = postService.addView(postId, userId, ip);
        return result == 1 ? Result.success(null) : Result.fail("添加失败");
    }
}
