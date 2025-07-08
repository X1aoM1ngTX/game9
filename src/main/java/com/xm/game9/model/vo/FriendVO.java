package com.xm.game9.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 好友信息视图
 *
 * @author X1aoM1ngTX
 */
@Data
public class FriendVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 好友用户名
     */
    private String userName;

    /**
     * 好友昵称
     */
    private String userNickname;

    /**
     * 好友头像
     */
    private String userAvatar;

    /**
     * 好友简介
     */
    private String userProfile;

    /**
     * 好友备注
     */
    private String remark;

    /**
     * 关系创建时间
     */
    private Date createTime;

    /**
     * 是否在线
     */
    private Boolean isOnline;
} 