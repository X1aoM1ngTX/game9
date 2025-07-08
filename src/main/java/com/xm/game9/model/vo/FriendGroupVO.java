package com.xm.game9.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 好友分组视图对象
 *
 * @author X1aoM1ngTX
 */
@Data
public class FriendGroupVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分组ID
     */
    private Long groupId;
    
    /**
     * 分组名称
     */
    private String groupName;
    
    /**
     * 分组中的好友数量
     */
    private Integer friendCount;
    
    /**
     * 分组创建时间
     */
    private Date groupCreateTime;
    
    /**
     * 分组中的好友列表（可选）
     */
    private List<FriendVO> friends;
}