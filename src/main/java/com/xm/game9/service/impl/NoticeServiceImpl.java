package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.NoticeMapper;
import com.xm.game9.model.domain.Notice;
import com.xm.game9.model.request.notice.NoticeCreateRequest;
import com.xm.game9.service.NoticeService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author XMTX8yyds
 * @description 针对表【notice(公告表)】的数据库操作Service实现
 * @createDate 2025-04-16 12:01:13
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
        implements NoticeService {

    /**
     * 新增公告
     *
     * @param noticeCreateRequest 公告创建请求（包含创建者ID）
     * @return 新创建的公告ID
     */
    @Override
    public Long createNotice(NoticeCreateRequest noticeCreateRequest) {
        if (noticeCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String title = noticeCreateRequest.getNoticeTitle();
        String content = noticeCreateRequest.getNoticeContent();
        Integer type = noticeCreateRequest.getNoticeType();
        Date expireTime = noticeCreateRequest.getNoticeExpireTime();

        // 校验参数
        if (title == null || title.length() > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告标题不能为空且字符数不能超过255");
        }
        if (content == null || content.length() > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告内容不能为空且字符数不能超过10000");
        }
        if (type == null || type < 0 || type > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告类型非法");
        }
        if (expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告过期时间非法");
        }

        Notice notice = new Notice();
        notice.setNoticeTitle(noticeCreateRequest.getNoticeTitle());
        notice.setNoticeContent(noticeCreateRequest.getNoticeContent());
        notice.setNoticeType(noticeCreateRequest.getNoticeType());
        notice.setNoticeExpireTime(noticeCreateRequest.getNoticeExpireTime());
        notice.setNoticeStatus(0); // 默认为草稿状态
        notice.setNoticeIsDelete(0); // 默认未删除
        notice.setNoticeCreateTime(new Date()); // 设置创建时间

        // 如果请求中包含创建者ID，则设置
        if (noticeCreateRequest.getNoticeCreatorId() != null) {
            notice.setNoticeCreatorId(noticeCreateRequest.getNoticeCreatorId());
        }

        boolean saveResult = save(notice);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "公告保存失败");
        }

        return notice.getNoticeId();
    }

    /**
     * 更新公告信息
     *
     * @param id     公告ID
     * @param notice 要更新的公告信息
     * @return 更新后的公告
     */
    @Override
    public Notice updateNotice(Long id, Notice notice) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告ID不合法");
        }
        if (notice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告内容为空");
        }

        Notice existingNotice = getById(id);
        if (existingNotice == null || existingNotice.getNoticeIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        }

        // 更新公告信息
        if (notice.getNoticeTitle() != null) {
            // 验证标题长度
            if (notice.getNoticeTitle().length() > 255) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告标题字符数不能超过255");
            }
            existingNotice.setNoticeTitle(notice.getNoticeTitle());
        }
        if (notice.getNoticeContent() != null) {
            // 验证内容长度
            if (notice.getNoticeContent().length() > 10000) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告内容字符数不能超过10000");
            }
            existingNotice.setNoticeContent(notice.getNoticeContent());
        }
        if (notice.getNoticeType() != null) {
            // 验证类型合法性
            if (notice.getNoticeType() < 0 || notice.getNoticeType() > 3) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告类型非法");
            }
            existingNotice.setNoticeType(notice.getNoticeType());
        }
        if (notice.getNoticeExpireTime() != null) {
            // 验证过期时间合法性
            if (notice.getNoticeExpireTime().before(new Date())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告过期时间非法");
            }
            existingNotice.setNoticeExpireTime(notice.getNoticeExpireTime());
        }

        boolean updated = updateById(existingNotice);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新公告失败");
        }

        return existingNotice;
    }

    /**
     * 获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @Override
    public Notice getNoticeById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告ID不合法");
        }

        Notice notice = getById(id);
        if (notice == null || notice.getNoticeIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在或已删除");
        }

        return notice;
    }

    /**
     * 获取所有已发布且未过期的公告
     *
     * @return 公告列表
     */
    @Override
    public List<Notice> getActiveNotices() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getNoticeStatus, 1) // 已发布
                .eq(Notice::getNoticeIsDelete, 0) // 未删除
                .gt(Notice::getNoticeExpireTime, new Date()) // 未过期
                .orderByDesc(Notice::getNoticePublishTime); // 按发布时间降序
        return list(wrapper);
    }

    /**
     * 按类型获取所有已发布且未过期的公告
     *
     * @param type 公告类型
     * @return 公告列表
     */
    @Override
    public List<Notice> getActiveNoticesByType(Integer type) {
        if (type == null || type < 0 || type > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告类型参数错误");
        }

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getNoticeStatus, 1) // 已发布
                .eq(Notice::getNoticeIsDelete, 0) // 未删除
                .eq(Notice::getNoticeType, type) // 指定类型
                .gt(Notice::getNoticeExpireTime, new Date()) // 未过期
                .orderByDesc(Notice::getNoticePublishTime); // 按发布时间降序
        return list(wrapper);
    }

    /**
     * 获取公告分页数据
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param status    公告状态
     * @param type      公告类型
     * @param creatorId 创建者ID
     * @return 公告分页数据
     */
    @Override
    public Page<Notice> getNoticePage(Integer pageNum, Integer pageSize, Integer status, Integer type, Long creatorId) {
        // 参数校验
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        if (status != null && (status < 0 || status > 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告状态参数错误");
        }
        if (type != null && (type < 0 || type > 3)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告类型参数错误");
        }

        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();

        // 只查询未删除的公告
        wrapper.eq(Notice::getNoticeIsDelete, 0);

        // 根据状态筛选
        if (status != null) {
            wrapper.eq(Notice::getNoticeStatus, status);
        }

        // 根据类型筛选
        if (type != null) {
            wrapper.eq(Notice::getNoticeType, type);
        }

        // 根据创建者筛选
        if (creatorId != null) {
            wrapper.eq(Notice::getNoticeCreatorId, creatorId);
        }

        // 如果是查询已发布的公告，只显示未过期的
        if (status != null && status == 1) {
            wrapper.gt(Notice::getNoticeExpireTime, new Date());
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(Notice::getNoticeCreateTime);

        return page(page, wrapper);
    }

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 是否发布成功
     */
    @Override
    public boolean publishNotice(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告ID不合法");
        }

        // 检查公告是否存在
        Notice existingNotice = getById(id);
        if (existingNotice == null || existingNotice.getNoticeIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在或已删除");
        }

        // 检查是否已经是发布状态
        if (existingNotice.getNoticeStatus() == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告已经是发布状态");
        }

        Notice notice = new Notice();
        notice.setNoticeId(id);
        notice.setNoticeStatus(1); // 设置为已发布状态
        notice.setNoticePublishTime(new Date()); // 设置发布时间

        boolean result = updateById(notice);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发布失败");
        }

        return true;
    }

    /**
     * 将公告设为草稿
     *
     * @param id 公告ID
     * @return 是否设置成功
     */
    @Override
    public boolean draftNotice(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告ID不合法");
        }

        // 检查公告是否存在
        Notice existingNotice = getById(id);
        if (existingNotice == null || existingNotice.getNoticeIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在或已删除");
        }

        // 检查是否已经是草稿状态
        if (existingNotice.getNoticeStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告已经是草稿状态");
        }

        Notice notice = new Notice();
        notice.setNoticeId(id);
        notice.setNoticeStatus(0); // 设置为草稿状态

        boolean result = updateById(notice);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "设置草稿失败");
        }

        return true;
    }

    /**
     * 逻辑删除公告
     *
     * @param id 公告ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteNotice(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告ID不合法");
        }

        // 检查公告是否存在
        Notice existingNotice = getById(id);
        if (existingNotice == null || existingNotice.getNoticeIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在或已删除");
        }

        Notice notice = new Notice();
        notice.setNoticeId(id);
        notice.setNoticeIsDelete(1); // 设置为已删除状态

        boolean result = updateById(notice);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }

        return true;
    }

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     * @return 是否全部删除成功
     */
    @Override
    public boolean batchDeleteNotices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID列表不能为空");
        }

        boolean allSuccess = true;
        for (Long id : ids) {
            if (id == null || id <= 0) {
                continue;
            }

            // 检查公告是否存在
            Notice existingNotice = getById(id);
            if (existingNotice == null || existingNotice.getNoticeIsDelete() == 1) {
                continue;
            }

            boolean success = deleteNotice(id);
            if (!success) {
                allSuccess = false;
            }
        }

        if (!allSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部分公告删除失败");
        }

        return true;
    }
}




