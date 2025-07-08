package com.xm.game9.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class FriendRequestVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;           // 申请记录ID
    private String username;   // 申请人用户名
    private String avatar;     // 申请人头像
    private String remark;     // 申请备注
    private Integer status;    // 0: 待处理, 1: 已接受, 2: 已拒绝
    private Date createTime;   // 申请时间
} 