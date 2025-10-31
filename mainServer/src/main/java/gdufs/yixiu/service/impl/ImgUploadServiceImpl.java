package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.UsersMapper;
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

@Slf4j
@Service
public class ImgUploadServiceImpl implements ImgUploadService {

    private String avatarPath;

    @Value("${resources-path.avatar}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    @Autowired
    private UsersMapper userMapper;
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
}
