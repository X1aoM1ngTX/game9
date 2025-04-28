package com.xm.gamehub.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 公告表
 *
 * @TableName notice
 */
@TableName(value = "notice")
@Data
public class Notice {
    /**
     * 公告Id
     */
    @TableId(type = IdType.AUTO)
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告类型（0-普通，1-重要，2-系统，3-活动）
     */
    private Integer noticeType;

    /**
     * 公告状态（0-草稿，1-已发布）
     */
    private Integer noticeStatus;

    /**
     * 创建者
     */
    private Long noticeCreatorId;

    /**
     * 创建时间
     */
    private Date noticeCreateTime;

    /**
     * 发布时间
     */
    private Date noticePublishTime;

    /**
     * 过期时间
     */
    private Date noticeExpireTime;

    /**
     * 是否删除
     */
    private Integer noticeIsDelete;
}