package com.xm.game9.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.News;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.news.NewsCreateRequest;
import com.xm.game9.model.request.news.NewsUpdateRequest;
import com.xm.game9.service.NewsService;
import com.xm.game9.service.UserService;
import com.xm.game9.utils.UploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.xm.game9.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 资讯管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/news")
@Tag(name = "资讯接口", description = "资讯相关的所有接口")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true") // 根据前端调整
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UploadUtil uploadUtil;

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求
     * @return 当前登录用户，未登录则抛出异常
     */
    private User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        return (User) userObj;
    }

    // ===== 资讯操作统一接口（所有登录用户可用） =====

    /**
     * 创建资讯
     */
    @Operation(summary = "创建资讯", description = "所有登录用户创建一个新的资讯")
    @PostMapping("/create")
    public BaseResponse<Long> createNews(@RequestBody NewsCreateRequest newsCreateRequest, HttpServletRequest request) {
        if (newsCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = getLoginUser(request);
        News news = new News();
        BeanUtils.copyProperties(newsCreateRequest, news);
        Long newsId = newsService.createNews(news, loginUser.getUserId());
        log.info("用户 {} 创建资讯成功, ID: {}", loginUser.getUserName(), newsId);
        return ResultUtils.success(newsId);
    }

    /**
     * 更新资讯
     */
    @Operation(summary = "更新资讯", description = "登录用户只能更新自己的资讯")
    @PutMapping("/update/{id}")
    public BaseResponse<Boolean> updateNews(
            @Parameter(description = "资讯ID", required = true) @PathVariable Long id,
            @RequestBody NewsUpdateRequest newsUpdateRequest,
            HttpServletRequest request) {
        if (id == null || id <= 0 || newsUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 获取当前登录用户
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getUserId();

        // 获取资讯信息
        News newsToUpdate = newsService.getNewsById(id);
        if (newsToUpdate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在");
        }

        // 检查是否为资讯作者
        if (!userId.equals(newsToUpdate.getNewsAuthorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能更新自己创建的资讯");
        }

        BeanUtils.copyProperties(newsUpdateRequest, newsToUpdate, "newsId", "authorId", "newsCreatedTime", "newsViews", "newsStatus", "isDelete");
        newsToUpdate.setNewsId(id);
        boolean success = newsService.updateNews(newsToUpdate);
        return ResultUtils.success(success);
    }

    /**
     * 上传资讯封面
     *
     * @param file    资讯封面文件
     * @param request HttpServletRequest
     * @return 资讯封面访问路径
     */
    @Operation(summary = "上传资讯封面", description = "上传资讯封面")
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }
        getLoginUser(request); // 验证用户登录状态

        try {
            // 确保上传服务可用
            if (uploadUtil == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传服务未初始化");
            }

            String url = uploadUtil.uploadR2(file);
            return ResultUtils.success(url);
        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getDetailMessage());
            throw e;
        } catch (java.io.IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传过程中发生未知错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败，请稍后重试");
        }
    }

    /**
     * 删除资讯
     */
    @Operation(summary = "删除资讯", description = "登录用户只能删除自己发布的资讯")
    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteNews(
            @Parameter(description = "资讯ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID无效");
        }

        // 获取当前登录用户
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getUserId();

        // 获取资讯信息
        News news = newsService.getNewsById(id);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在");
        }

        // 检查是否为资讯作者
        if (!userId.equals(news.getNewsAuthorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能删除自己发布的资讯");
        }

        // 执行删除操作
        boolean success = newsService.deleteNews(id);
        return ResultUtils.success(success);
    }

    /**
     * 发布资讯
     */
    @Operation(summary = "发布资讯", description = "登录用户只能发布自己的资讯")
    @PostMapping("/publish/{id}")
    public BaseResponse<Boolean> publishNews(
            @Parameter(description = "资讯ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID无效");
        }

        // 获取当前登录用户
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getUserId();

        // 获取资讯信息
        News news = newsService.getNewsById(id);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在");
        }

        // 检查是否为资讯作者
        if (!userId.equals(news.getNewsAuthorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能发布自己创建的资讯");
        }

        boolean success = newsService.publishNews(id);
        return ResultUtils.success(success);
    }

    /**
     * 将资讯设为草稿
     */
    @Operation(summary = "将资讯设为草稿", description = "登录用户只能操作自己的资讯")
    @PostMapping("/draft/{id}")
    public BaseResponse<Boolean> draftNews(
            @Parameter(description = "资讯ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID无效");
        }

        // 获取当前登录用户
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getUserId();

        // 获取资讯信息
        News news = newsService.getNewsById(id);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在");
        }

        // 检查是否为资讯作者
        if (!userId.equals(news.getNewsAuthorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能操作自己创建的资讯");
        }

        boolean success = newsService.draftNews(id);
        return ResultUtils.success(success);
    }

    /**
     * 分页查询资讯列表（可选条件：状态、作者）
     */
    @Operation(summary = "分页查询资讯列表", description = "分页查询所有资讯列表")
    @GetMapping("/list")
    public BaseResponse<Page<News>> listNews(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "资讯状态：0-草稿，1-已发布") @RequestParam(required = false) Integer status,
            @Parameter(description = "作者ID") @RequestParam(required = false) Long authorId) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        Page<News> page = newsService.getNewsPage(pageNum, pageSize, status, authorId);
        return ResultUtils.success(page);
    }

    /**
     * 获取已发布的资讯列表（分页）
     */
    @Operation(summary = "获取已发布的资讯列表", description = "分页查询已发布的资讯列表")
    @GetMapping("/list/published")
    public BaseResponse<Page<News>> listPublishedNews(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") Integer pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        Page<News> page = newsService.getNewsPage(pageNum, pageSize, 1, null);
        return ResultUtils.success(page);
    }

    /**
     * 获取当前用户的草稿资讯列表
     */
    @Operation(summary = "获取当前用户的草稿资讯列表", description = "分页查询当前登录用户的草稿资讯")
    @GetMapping("/my/drafts")
    public BaseResponse<Page<News>> listMyDrafts(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        User loginUser = getLoginUser(request);
        Page<News> page = newsService.getUserNewsPage(pageNum, pageSize, 0, loginUser.getUserId());
        return ResultUtils.success(page);
    }

    /**
     * 获取当前用户的已发布资讯列表
     */
    @Operation(summary = "获取当前用户的已发布资讯列表", description = "分页查询当前登录用户的已发布资讯")
    @GetMapping("/my/published")
    public BaseResponse<Page<News>> listMyPublishedNews(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        User loginUser = getLoginUser(request);
        Page<News> page = newsService.getUserNewsPage(pageNum, pageSize, 1, loginUser.getUserId());
        return ResultUtils.success(page);
    }

    /**
     * 获取资讯详情
     */
    @Operation(summary = "获取资讯详情", description = "获取指定已发布资讯的详细信息")
    @GetMapping("/get/{id}")
    public BaseResponse<News> getNewsDetail(
            @Parameter(description = "资讯ID", required = true) @PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID无效");
        }
        News news = newsService.getNewsById(id);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在");
        }
        if (news.getNewsStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯未发布或不存在");
        }
        newsService.incrementNewsViews(id);
        return ResultUtils.success(news);
    }

    /**
     * 获取草稿资讯详情
     */
    @Operation(summary = "获取草稿资讯详情", description = "获取指定草稿资讯的详细信息")
    @GetMapping("/get/draft/{id}")
    public BaseResponse<News> getDraftNews(
            @Parameter(description = "资讯ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "资讯ID无效");
        }
        User loginUser = getLoginUser(request);
        News news = newsService.getNewsById(id);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资讯不存在");
        }
        if (news.getNewsStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该资讯不是草稿状态");
        }
        if (!news.getNewsAuthorId().equals(loginUser.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权查看他人草稿");
        }
        return ResultUtils.success(news);
    }
}