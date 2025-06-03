package com.xm.game9.model.request.friend;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加好友请求
 *
 * @author X1aoM1ngTX
 */
@Data
public class FriendAddRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 好友备注
     */
    private String remark;
} 