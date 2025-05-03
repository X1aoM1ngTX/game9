package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.News;
// import com.xm.game9.model.request.news.NewsCreateRequest; // 未来可能需要创建
// import com.xm.game9.model.request.news.NewsUpdateRequest; // 未来可能需要创建

import java.util.List;

/**
 * @description 针对表【news(资讯表)】的数据库操作Service
 * @createDate 2024-06-18
 */
public interface NewsService extends IService<News> {

    /**
     * 创建资讯
     *
     * @param news 资讯数据
     * @param authorId 作者ID
     * @return 新创建的资讯ID
     */
    Long createNews(News news, Long authorId);

    /**
     * 更新资讯信息
     *
     * @param news 要更新的资讯信息
     * @return 是否成功
     */
    boolean updateNews(News news);

    /**
     * 获取资讯详情
     *
     * @param id 资讯ID
     * @return 资讯详情
     */
    News getNewsById(Long id);

    /**
     * 发布资讯
     *
     * @param id 资讯ID
     * @return 是否成功
     */
    boolean publishNews(Long id);

    /**
     * 将资讯设为草稿
     *
     * @param id 资讯ID
     * @return 是否成功
     */
    boolean draftNews(Long id);

    /**
     * 获取已发布的资讯列表（按发布时间降序）
     *
     * @return 资讯列表
     */
    List<News> getPublishedNews();

    /**
     * 分页获取资讯列表，支持多条件筛选
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态 (0-草稿, 1-已发布)
     * @param authorId 作者ID (可选)
     * @return 分页结果
     */
    Page<News> getNewsPage(Integer pageNum, Integer pageSize, Integer status, Long authorId);
    
    /**
     * 获取指定用户的指定状态的资讯列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态 (0-草稿, 1-已发布)
     * @param userId   用户ID
     * @return 分页结果
     */
    Page<News> getUserNewsPage(Integer pageNum, Integer pageSize, Integer status, Long userId);

    /**
     * 逻辑删除资讯
     *
     * @param id 资讯ID
     * @return 是否成功
     */
    boolean deleteNews(Long id);

    /**
     * 增加资讯浏览次数
     *
     * @param id 资讯ID
     */
    void incrementNewsViews(Long id);

}