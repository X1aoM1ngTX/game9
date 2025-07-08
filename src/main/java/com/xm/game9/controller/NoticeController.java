package com.xm.game9.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.Notice;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.notice.NoticeCreateRequest;
import com.xm.game9.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.xm.game9.constant.UserConstant.ADMIN_ROLE;
import static com.xm.game9.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 公告管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/notices")
@Tag(name = "公告接口", description = "公告相关的所有接口")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 创建公告
     *
     * @param noticeCreateRequest 公告创建请求
     * @param request             HTTP请求
     * @return 创建后的公告ID
     */
    @Operation(summary = "创建公告", description = "创建一个新的公告")
    @PostMapping("/create")
    public BaseResponse<Long> createNotice(@RequestBody NoticeCreateRequest noticeCreateRequest, HttpServletRequest request) {
        // 验证管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        // 获取当前登录用户并设置创建者ID
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        noticeCreateRequest.setNoticeCreatorId(loginUser.getUserId());

        // 调用服务创建公告
        Long noticeId = noticeService.createNotice(noticeCreateRequest);

        log.info("创建公告成功, ID: {}", noticeId);
        return ResultUtils.success(noticeId);
    }

    /**
     * 更新公告
     *
     * @param id      公告ID
     * @param notice  公告信息
     * @param request HTTP请求
     * @return 更新后的公告
     */
    @Operation(summary = "更新公告", description = "更新指定公告的信息")
    @PutMapping("/update/{id}")
    public BaseResponse<Notice> updateNotice(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id,
            @Validated @RequestBody Notice notice,
            HttpServletRequest request) {
        // 验证管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        log.info("更新公告, ID: {}", id);

        // 调用服务更新公告
        Notice updatedNotice = noticeService.updateNotice(id, notice);

        log.info("更新公告成功, ID: {}", id);
        return ResultUtils.success(updatedNotice);
    }

    /**
     * 逻辑删除公告
     *
     * @param id      公告ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "逻辑删除公告", description = "逻辑删除指定公告")
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Void> deleteNotice(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // 验证管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        log.info("删除公告, ID: {}", id);

        // 调用服务删除公告
        noticeService.deleteNotice(id);

        log.info("删除公告成功, ID: {}", id);
        return ResultUtils.success(null);
    }

    /**
     * 获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @Operation(summary = "获取公告详情", description = "获取指定公告的详细信息")
    @GetMapping("/get/{id}")
    public BaseResponse<Notice> getNotice(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id) {
        log.info("获取公告详情, ID: {}", id);

        // 调用服务获取公告详情
        Notice notice = noticeService.getNoticeById(id);

        log.info("获取公告详情成功, ID: {}", id);
        return ResultUtils.success(notice);
    }

    /**
     * 分页查询公告列表
     *
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param status    公告状态
     * @param type      公告类型
     * @param creatorId 创建者ID
     * @return 公告列表
     */
    @Operation(summary = "分页查询公告列表", description = "分页查询公告列表")
    @GetMapping("/list")
    public BaseResponse<Page<Notice>> listNotices(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "公告状态：0-草稿，1-已发布") @RequestParam(required = false) Integer status,
            @Parameter(description = "公告类型：0-普通，1-重要，2-系统，3-活动") @RequestParam(required = false) Integer type,
            @Parameter(description = "创建者ID") @RequestParam(required = false) Long creatorId) {
        log.info("分页查询公告列表: pageNum={}, pageSize={}, status={}, type={}, creatorId={}",
                pageNum, pageSize, status, type, creatorId);

        // 调用服务获取分页数据
        Page<Notice> page = noticeService.getNoticePage(pageNum, pageSize, status, type, creatorId);

        log.info("分页查询公告列表成功, 总条数: {}", page.getTotal());
        return ResultUtils.success(page);
    }

    /**
     * 发布公告
     *
     * @param id      公告ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "发布公告", description = "发布指定公告")
    @PostMapping("/publish/{id}")
    public BaseResponse<Void> publishNotice(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // 验证管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        log.info("发布公告, ID: {}", id);

        // 调用服务发布公告
        noticeService.publishNotice(id);

        log.info("发布公告成功, ID: {}", id);
        return ResultUtils.success(null);
    }

    /**
     * 将公告设为草稿
     *
     * @param id      公告ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "将公告设为草稿", description = "将指定公告设为草稿")
    @PostMapping("/draft/{id}")
    public BaseResponse<Void> draftNotice(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // 验证管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        log.info("将公告设为草稿, ID: {}", id);

        // 调用服务将公告设为草稿
        noticeService.draftNotice(id);

        log.info("设置公告为草稿成功, ID: {}", id);
        return ResultUtils.success(null);
    }

    /**
     * 获取有效的公告列表（已发布且未过期的）
     *
     * @return 公告列表
     */
    @Operation(summary = "获取有效的公告列表", description = "获取已发布且未过期的公告列表")
    @GetMapping("/list/active")
    public BaseResponse<List<Notice>> getActiveNotices() {
        log.info("获取有效的公告列表");

        // 调用服务获取有效公告
        List<Notice> notices = noticeService.getActiveNotices();

        log.info("获取有效的公告列表成功, 数量: {}", notices.size());
        return ResultUtils.success(notices);
    }

    /**
     * 获取指定类型的有效公告
     *
     * @param type 公告类型
     * @return 公告列表
     */
    @Operation(summary = "获取指定类型的有效公告", description = "获取已发布且未过期的指定类型的公告列表")
    @GetMapping("/list/active/{type}")
    public BaseResponse<List<Notice>> getActiveNoticesByType(
            @Parameter(description = "公告类型：0-普通，1-重要，2-系统，3-活动", required = true) @PathVariable Integer type) {
        log.info("获取指定类型的有效公告, 类型: {}", type);

        // 调用服务获取指定类型的有效公告
        List<Notice> notices = noticeService.getActiveNoticesByType(type);

        log.info("获取指定类型的有效公告成功, 类型: {}, 数量: {}", type, notices.size());
        return ResultUtils.success(notices);
    }

    /**
     * 批量删除公告
     *
     * @param ids     公告ID列表
     * @param request HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "批量删除公告", description = "批量删除指定的多个公告")
    @DeleteMapping("/delete/batch")
    public BaseResponse<Void> batchDeleteNotices(
            @Parameter(description = "公告ID列表", required = true) @RequestParam List<Long> ids,
            HttpServletRequest request) {
        // 验证管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        log.info("批量删除公告, ID列表: {}", ids);

        // 调用服务批量删除公告
        noticeService.batchDeleteNotices(ids);

        log.info("批量删除公告成功, 数量: {}", ids.size());
        return ResultUtils.success(null);
    }

    /**
     * 判断是否为管理员
     *
     * @param request HTTP请求
     * @return boolean (是否为管理员)
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 获取当前登录用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            log.warn("用户未登录");
            return false;
        }
        User user = (User) userObj;
        // 判断是否为管理员
        return user.getUserIsAdmin() == ADMIN_ROLE;
    }
}