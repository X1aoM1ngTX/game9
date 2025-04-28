package com.xm.gamehub.model.request.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BatchImportUsersRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 515684984113287L;
    private List<UserImportInfo> users;

    @Data
    public static class UserImportInfo {
        private String userName;
        private String userEmail;
        private String userPassword;
        private String userPhone;
        private Integer userIsAdmin;
    }
} 