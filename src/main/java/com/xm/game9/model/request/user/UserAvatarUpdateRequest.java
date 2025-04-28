package com.xm.game9.model.request.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserAvatarUpdateRequest {
    private MultipartFile file;
} 