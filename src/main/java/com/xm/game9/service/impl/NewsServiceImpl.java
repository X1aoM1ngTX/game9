package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.NewsMapper;
import com.xm.game9.model.domain.News;
import com.xm.game9.service.NewsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author X1aoM1ngTX
 * @description 针对表【news(资讯表)】的数据库操作Service实现
 * @createDate 2025-04-30
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
        implements NewsService {

    @Resource
    private NewsMapper newsMapper;

    /**
     * 创建资讯
     *
     * @param news     资讯对象
     * @param authorId 作者ID
     * @return 资讯ID
     */
    @Override
    public Long createNews(News news, Long authorId) {
        if (news == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (authorId == null || authorId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "作者ID非法");
        }

        // 校验参数
        if (!StringUtils.hasText(news.getNewsTitle()) || news.getNewsTitle().length() > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯标题不能为空且长度不能超过255");
        }
        if (!StringUtils.hasText(news.getNewsContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯内容不能为空");
        }
        if (news.getNewsSummary() != null && news.getNewsSummary().length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯摘要长度不能超过500");
        }

        // 设置默认值和创建信息
        news.setNewsAuthorId(authorId);
        news.setNewsStatus(0);
        news.setNewsIsDelete(0);
        news.setNewsCreateTime(new Date());
        news.setNewsUpdateTime(new Date());
        news.setNewsViews(0);

        boolean saveResult = this.save(news);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "资讯保存失败");
        }
        return news.getNewsId();
    }

    /**
     * 更新资讯
     *
     * @param news 资讯对象
     * @return 是否成功
     */
    @Override
    public boolean updateNews(News news) {
        if (news == null || news.getNewsId() == null || news.getNewsId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID非法");
        }

        News existingNews = this.getById(news.getNewsId());
        if (existingNews == null || existingNews.getNewsIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在或已删除");
        }

        // 准备更新的对象
        News updateNews = new News();
        updateNews.setNewsId(news.getNewsId());

        // 校验并设置更新字段
        if (StringUtils.hasText(news.getNewsTitle())) {
            if (news.getNewsTitle().length() > 255) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯标题长度不能超过255");
            }
            updateNews.setNewsTitle(news.getNewsTitle());
        }
        if (StringUtils.hasText(news.getNewsContent())) {
            updateNews.setNewsContent(news.getNewsContent());
        }
        if (news.getNewsSummary() != null) {
            if (news.getNewsSummary().length() > 500) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯摘要长度不能超过500");
            }
            updateNews.setNewsSummary(news.getNewsSummary());
        }
        if (StringUtils.hasText(news.getNewsCoverImage())) {
            // 可添加URL格式校验
            updateNews.setNewsCoverImage(news.getNewsCoverImage());
        }

        updateNews.setNewsUpdateTime(new Date());

        boolean updateResult = this.updateById(updateNews);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新资讯失败");
        }
        return true;
    }

    /**
     * 根据资讯ID获取资讯
     *
     * @param id 资讯ID
     * @return 资讯对象
     */
    @Override
    public News getNewsById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID非法");
        }
        News news = this.getById(id);
        if (news == null || news.getNewsIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在或已删除");
        }
        return news;
    }

    /**
     * 发布资讯
     *
     * @param id 资讯ID
     * @return 是否成功
     */
    @Override
    public boolean publishNews(Long id) {
        News news = getNewsById(id);
        if (news.getNewsStatus() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "资讯已发布");
        }

        News updateNews = new News();
        updateNews.setNewsId(id);
        updateNews.setNewsStatus(1);
        updateNews.setNewsPublishTime(new Date());
        updateNews.setNewsUpdateTime(new Date());

        return this.updateById(updateNews);
    }

    /**
     * 草稿资讯
     *
     * @param id 资讯ID
     * @return 是否成功
     */
    @Override
    public boolean draftNews(Long id) {
        News news = getNewsById(id);
        if (news.getNewsStatus() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "资讯已经是草稿状态");
        }

        News updateNews = new News();
        updateNews.setNewsId(id);
        updateNews.setNewsStatus(0);
        updateNews.setNewsUpdateTime(new Date());
        // 发布时间不清空，保留历史记录

        return this.updateById(updateNews);
    }

    /**
     * 获取已发布的资讯
     *
     * @return 已发布的资讯列表
     */
    @Override
    public List<News> getPublishedNews() {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getNewsStatus, 1)
                .eq(News::getNewsIsDelete, 0)
                .orderByDesc(News::getNewsPublishTime);
        return this.list(wrapper);
    }

    /**
     * 获取资讯分页列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态
     * @param authorId 作者ID
     * @return 资讯分页列表
     */
    @Override
    public Page<News> getNewsPage(Integer pageNum, Integer pageSize, Integer status, Long authorId) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Page<News> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getNewsIsDelete, 0);

        if (status != null && (status == 0 || status == 1)) {
            wrapper.eq(News::getNewsStatus, status);
        }
        if (authorId != null && authorId > 0) {
            wrapper.eq(News::getNewsAuthorId, authorId);
        }

        // 默认按更新时间降序，已发布的按发布时间降序
        if (status != null && status == 1) {
            wrapper.orderByDesc(News::getNewsPublishTime);
        } else {
            wrapper.orderByDesc(News::getNewsUpdateTime);
        }

        return this.page(page, wrapper);
    }

    /**
     * 获取指定用户的指定状态的资讯列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态 (0-草稿, 1-已发布)
     * @param userId   用户ID
     * @return 分页结果
     */
    @Override
    public Page<News> getUserNewsPage(Integer pageNum, Integer pageSize, Integer status, Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID非法");
        }

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Page<News> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getNewsIsDelete, 0)
                .eq(News::getNewsAuthorId, userId);

        // 指定状态
        if (status != null) {
            wrapper.eq(News::getNewsStatus, status);
        }

        // 排序规则：已发布的按发布时间降序，草稿按更新时间降序
        if (status != null && status == 1) {
            wrapper.orderByDesc(News::getNewsPublishTime);
        } else {
            wrapper.orderByDesc(News::getNewsUpdateTime);
        }

        return this.page(page, wrapper);
    }

    /**
     * 删除资讯
     *
     * @param id 资讯ID
     * @return 是否成功
     */
    @Override
    public boolean deleteNews(Long id) {
        getNewsById(id);

        News updateNews = new News();
        updateNews.setNewsId(id);
        updateNews.setNewsIsDelete(1);
        updateNews.setNewsUpdateTime(new Date());

        return this.updateById(updateNews);
    }

    /**
     * 增加资讯浏览次数
     *
     * @param id 资讯ID
     */
    @Override
    public void incrementNewsViews(Long id) {
        if (id == null || id <= 0) {
            log.warn("Attempted to increment views for invalid news ID: " + id);
            return; // 或者抛出异常，取决于业务需求
        }
        // 使用 LambdaUpdateWrapper 原子性更新浏览次数
        LambdaUpdateWrapper<News> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(News::getNewsId, id)
                .setSql("newsViews = newsViews + 1");
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            // 可能资讯不存在或已被删除，记录日志
            log.warn("Failed to increment views for news ID: " + id + ". News might not exist or be deleted.");
        }
    }
}