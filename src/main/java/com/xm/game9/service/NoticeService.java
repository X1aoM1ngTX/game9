package com.xm.gamehub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.gamehub.model.domain.Notice;
import com.xm.gamehub.model.request.notice.NoticeCreateRequest;

import java.util.List;

/**
 * @author XMTX8yyds
 * @description 针对表【notice(公告表)】的数据库操作Service
 * @createDate 2025-04-16 12:01:13
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 创建公告
     *
     * @param noticeCreateRequest 公告创建请求
     * @return 新创建的公告ID
     */
    Long createNotice(NoticeCreateRequest noticeCreateRequest);

    /**
     * 更新公告信息
     *
     * @param id 公告ID
     * @param notice 要更新的公告信息
     * @return 更新后的公告
     */
    Notice updateNotice(Long id, Notice notice);
    
    /**
     * 获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    Notice getNoticeById(Long id);

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 是否成功
     */
    boolean publishNotice(Long id);

    /**
     * 将公告设为草稿
     *
     * @param id 公告ID
     * @return 是否成功
     */
    boolean draftNotice(Long id);
    
    /**
     * 获取有效的公告列表（已发布且未过期的）
     *
     * @return 公告列表
     */
    List<Notice> getActiveNotices();

    /**
     * 按类型获取指定类型的有效公告
     *
     * @param type 公告类型
     * @return 公告列表
     */
    List<Notice> getActiveNoticesByType(Integer type);

    /**
     * 分页获取公告列表，支持多条件筛选
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param status    状态
     * @param type      类型
     * @param creatorId 创建者ID
     * @return 分页结果
     */
    Page<Notice> getNoticePage(Integer pageNum, Integer pageSize, Integer status, Integer type, Long creatorId);

    /**
     * 逻辑删除公告
     *
     * @param id 公告ID
     * @return 是否成功
     */
    boolean deleteNotice(Long id);
    
    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     * @return 是否全部删除成功
     */
    boolean batchDeleteNotices(List<Long> ids);
}
