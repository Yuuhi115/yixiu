package gdufs.yixiu.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImgUploadService {
    String uploadAvatar (MultipartFile file, int id);
    String uploadRequestImg (MultipartFile file, Integer requestId, Integer number);
    String uploadRepairLogImg (MultipartFile file, Integer logId, Integer number);
    String uploadPostImg (MultipartFile file, Integer postId, Integer number);
    List<String> deletePostImg (Integer postId);
}
