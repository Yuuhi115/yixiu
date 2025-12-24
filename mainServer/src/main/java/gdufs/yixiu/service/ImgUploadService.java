package gdufs.yixiu.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImgUploadService {
    String uploadAvatar (MultipartFile file, int id);
    String uploadRequestImg (MultipartFile file, Integer requestId, Integer number);
    String uploadRepairLogImg (MultipartFile file, Integer logId, Integer number);
}
