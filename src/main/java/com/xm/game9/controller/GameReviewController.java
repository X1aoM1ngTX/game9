package com.xm.game9.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.game.GameReviewRequest;
import com.xm.game9.model.request.game.GameReviewUpdateRequest;
import com.xm.game9.model.vo.GameReviewVO;
import com.xm.game9.service.GameReviewService;
import com.xm.game9.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏评价接口
 *
 * @author X1aoM1ngTX
 */
@Tag(name = "游戏评价接口", description = "游戏评价相关的所有接口")
@RestController
@RequestMapping("/game/review")
@Slf4j
public class GameReviewController {

    @Resource
    private GameReviewService gameReviewService;

    @Resource
    private UserService userService;

    /**
     * 添加游戏评价
     *
     * @param gameReviewRequest 评价请求
     * @param request           HTTP请求
     * @return 评价ID
     */
    @Operation(summary = "添加游戏评价", description = "添加新的游戏评价")
    @PostMapping("/add")
    public BaseResponse<Long> createReview(@RequestBody GameReviewRequest gameReviewRequest, HttpServletRequest request) {
        if (gameReviewRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        Long result = gameReviewService.createReview(loginUser.getUserId(), gameReviewRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取游戏的所有评价
     *
     * @param gameId 游戏ID
     * @return 评价列表
     */
    @Operation(summary = "获取游戏评价", description = "获取指定游戏的所有评价")
    @GetMapping("/list")
    public BaseResponse<List<GameReviewVO>> getGameReviews(@RequestParam Long gameId) {
        if (gameId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        List<GameReviewVO> reviews = gameReviewService.getGameReviews(gameId);
        return ResultUtils.success(reviews);
    }

    /**
     * 分页获取游戏评价
     *
     * @param gameId   游戏ID
     * @param current  当前页
     * @param pageSize 每页大小
     * @return 分页评价列表
     */
    @Operation(summary = "分页获取游戏评价", description = "分页获取指定游戏的评价")
    @GetMapping("/page")
    public BaseResponse<Page<GameReviewVO>> pageGameReviews(
            @RequestParam Long gameId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize) {
        if (gameId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Page<GameReviewVO> reviews = gameReviewService.pageGameReviews(gameId, current, pageSize);
        return ResultUtils.success(reviews);
    }

    /**
     * 删除评价
     *
     * @param reviewId 评价ID
     * @param request  HTTP请求
     * @return 是否删除成功
     */
    @Operation(summary = "删除评价", description = "删除指定的游戏评价")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteReview(@RequestParam Long reviewId, HttpServletRequest request) {
        if (reviewId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = gameReviewService.deleteReview(reviewId, loginUser.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 获取游戏的平均评分
     *
     * @param gameId 游戏ID
     * @return 平均评分
     */
    @Operation(summary = "获取游戏平均评分", description = "获取指定游戏的平均评分")
    @GetMapping("/average")
    public BaseResponse<Double> getGameAverageRating(@RequestParam Long gameId) {
        if (gameId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Double averageRating = gameReviewService.getGameAverageRating(gameId);
        return ResultUtils.success(averageRating);
    }

    /**
     * 修改评价
     *
     * @param gameReviewUpdateRequest 评价更新请求
     * @param request                 HTTP请求
     * @return 是否修改成功
     */
    @Operation(summary = "修改评价", description = "修改指定的游戏评价")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateReview(@RequestBody GameReviewUpdateRequest gameReviewUpdateRequest, HttpServletRequest request) {
        if (gameReviewUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = gameReviewService.updateReview(loginUser.getUserId(), gameReviewUpdateRequest);
        return ResultUtils.success(result);
    }
} 