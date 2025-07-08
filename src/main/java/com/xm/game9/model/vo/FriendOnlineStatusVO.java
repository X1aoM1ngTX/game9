package com.xm.game9.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友在线状态视图对象
 *
 * @author X1aoM1ngTX
 */
@Data
public class FriendOnlineStatusVO implements Serializable {

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
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 最后在线时间
     */
    private Date lastOnlineTime;

    /**
     * 在线状态描述
     */
    private String onlineStatus;
}