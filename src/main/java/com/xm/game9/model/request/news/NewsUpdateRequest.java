package com.xm.game9.model.request.news;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新资讯请求体
 */
@Data
public class NewsUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资讯ID (必须)
     */
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
     * 资讯摘要
     */
    private String newsSummary;

    /**
     * 资讯封面图片URL
     */
    private String newsCoverImage;

  /**
     * 游戏标签ID (关联游戏表)
     */
    private Long newsGameTag;

    /**
     * 自定义标签（JSON格式存储多个标签）
     */
    private String newsCustomTags;

}