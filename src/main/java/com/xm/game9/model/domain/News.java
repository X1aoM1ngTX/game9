package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 资讯表
 *
 * @TableName news
 */
@TableName(value = "news")
@Data
public class News implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 资讯ID
     */
    @TableId(type = IdType.AUTO)
    private Long newsId;

    /**
     * 资讯标题
     */
    private String newsTitle;

    /**
     * 资讯内容
     */
    private String newsContent;

    /**
     * 作者ID (关联用户表)
     */
    private Long newsAuthorId;

    /**
     * 资讯状态（0-草稿，1-已发布）
     */
    private Integer newsStatus;

    /**
     * 创建时间
     */
    private Date newsCreateTime;

    /**
     * 更新时间
     */
    private Date newsUpdateTime;

    /**
     * 发布时间
     */
    private Date newsPublishTime;

    /**
     * 是否删除 (0-未删除, 1-已删除)
     */
    private Integer newsIsDelete;

    /**
     * 资讯封面图 URL
     */
    private String newsCoverImage;

    /**
     * 资讯摘要
     */
    private String newsSummary;

    /**
     * 浏览次数
     */
    private Integer newsViews;

    /**
     * 游戏标签ID (关联游戏表)
     */
    private Long newsGameTag;

    /**
     * 游戏标签名称
     */
    private String newsGameTagName;

    /**
     * 自定义标签（JSON格式存储多个标签）
     */
    private String newsCustomTags;
}