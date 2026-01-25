package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.PostMapper;
import gdufs.yixiu.dao.TaskMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.pojo.PostImg;
import gdufs.yixiu.pojo.RepairLogImg;
import gdufs.yixiu.pojo.RepairRequestImg;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.ImgUploadService;
import gdufs.yixiu.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ImgUploadServiceImpl implements ImgUploadService {

    private String avatarPath;
    private String requestPath;
    private String repairLogPath;
    private String postImgPath;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UsersMapper userMapper;

    @Value("${resources-path.avatar}")
//    @Value("${resources-path.linux_avatar}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    @Value("${resources-path.request}")
//    @Value("${resources-path.linux_request}")
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
    @Value("${resources-path.repairLog}")
    public void setRepairLogPath(String repairLogPath) {
        this.repairLogPath = repairLogPath;
    }
    @Value("${resources-path.postImg}")
    public void setPostImgPath(String postImgPath) {
        this.postImgPath = postImgPath;
    }

    /*上传用户头像到本地*/
    @Override
    public String uploadAvatar(MultipartFile file, int id) {
        log.info("Uploading No.{} user's avatar to {}", id, avatarPath);
        String originalFilename = file.getOriginalFilename();

        // 显示文件大小（以MB为单位）
        long fileSizeInBytes = file.getSize();
        double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
        String formattedFileSize = String.format("%.2f", fileSizeInMB);
        log.info("File size: {} MB", formattedFileSize);

        String fileName = "user_" + id + "_avatar" + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        Path uploadPath = Paths.get(avatarPath);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            Users user = new Users();
            user.setUserId(id);
            user.setAvatar(fileName);
            userMapper.updateUser(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("No.{} user update avatar success", id);
        return avatarPath + fileName;
    }

    @Override
    public String uploadRequestImg(MultipartFile file, Integer requestId, Integer number) {
        log.info("Uploading No.{} request's img to {}", requestId, requestPath);
        String originalFilename = file.getOriginalFilename();

        // 显示文件大小（以MB为单位）
        long fileSizeInBytes = file.getSize();
        double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
        String formattedFileSize = String.format("%.2f", fileSizeInMB);
        log.info("requestId:{}, imgNumber:{}, file size: {} MB",requestId, number, formattedFileSize);
        String fileName = "request_" + requestId + "_img_" + number + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        Path uploadPath = Paths.get(requestPath);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            RepairRequestImg img = new RepairRequestImg();
            img.setRequestId(requestId);
            img.setImgUrl("request/" + fileName);
            taskMapper.addRequestImg(img);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "request/" + fileName;
    }

    @Override
    public String uploadRepairLogImg(MultipartFile file, Integer logId, Integer number) {
        log.info("Uploading No.{} repair log's img to {}", logId, repairLogPath);
        String originalFilename = file.getOriginalFilename();
        // 显示文件大小（以MB为单位）
        long fileSizeInBytes = file.getSize();
        double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
        String formattedFileSize = String.format("%.2f", fileSizeInMB);
        log.info("logId:{}, imgNumber:{}, file size: {} MB",logId, number, formattedFileSize);
        String fileName = "repairLog_" + logId + "_img_" + number + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        Path uploadPath = Paths.get(repairLogPath);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            RepairLogImg img = new RepairLogImg();
            img.setLogId(logId);
            img.setImgUrl("repairLog/" + fileName);
            taskMapper.addTaskLogImg(img);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "repairLog/" + fileName;
    }

    @Override
    public String uploadPostImg(MultipartFile file, Integer postId, Integer number) {
        log.info("Uploading No.{} post's img to {}", postId, postImgPath);
        String originalFilename = file.getOriginalFilename();
        // 显示文件大小（以MB为单位）
        long fileSizeInBytes = file.getSize();
        double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
        String formattedFileSize = String.format("%.2f", fileSizeInMB);
        log.info("postId:{}, imgNumber:{}, file size: {} MB",postId, number, formattedFileSize);
        String fileName = "post_" + postId + "_img_" + number + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        Path uploadPath = Paths.get(postImgPath);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            PostImg img = new PostImg();
            img.setPostId(postId);
            img.setImgUrl("postImg/" + fileName);
            postMapper.addPostImg(img);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "postImg/" + fileName;
    }

    @Override
    public List<String> deletePostImg(Integer postId) {
        log.info("Deleting No.{} post's img", postId);
        List<String> imgUrls = postMapper.queryPostImgUrls(postId);
        List<String> deleteImgUrls = new ArrayList<>();
        for (String imgUrl : imgUrls) {
            Path path = Paths.get(postImgPath + imgUrl.substring(imgUrl.lastIndexOf("/") + 1));
            try {
                Files.deleteIfExists(path);
                deleteImgUrls.add(imgUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Deleted No.{} post's img, file: {} has been deleted", postId, deleteImgUrls);
        return deleteImgUrls;
    }
}
