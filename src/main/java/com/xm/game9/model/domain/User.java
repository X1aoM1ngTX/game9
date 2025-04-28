package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @表名 user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户邮箱
     */
    private String userEmail;
    /**
     * 用户电话
     */
    private String userPhone;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 用户创建时间
     */
    private Date userCreatedTime;
    /**
     * 用户是否已删除
     */
    @TableLogic
    private Integer userIsDelete;
    /**
     * 用户是否为管理员
     */
    private Integer userIsAdmin;
    /**
     * 用户简介
     */
    private String userProfile;
    /**
     * 用户头像URL
     */
    private String userAvatar;
    /**
     * 用户昵称
     */
    private String userNickname;
}