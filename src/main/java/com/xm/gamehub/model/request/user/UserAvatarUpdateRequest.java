package com.xm.gamehub.model.request.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserAvatarUpdateRequest {
    private MultipartFile file;
} 