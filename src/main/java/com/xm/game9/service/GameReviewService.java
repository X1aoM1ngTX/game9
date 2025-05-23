package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.GameReview;
import com.xm.game9.model.request.game.GameReviewRequest;
import com.xm.game9.model.request.game.GameReviewUpdateRequest;
import com.xm.game9.model.vo.GameReviewVO;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @description 针对表【gameReview(游戏评价表)】的数据库操作Service
 * @createDate 2024-03-19
 */
public interface GameReviewService extends IService<GameReview> {

    /**
     * 创建游戏评价
     *
     * @param userId            用户ID
     * @param gameReviewRequest 评价请求
     * @return 评价ID
     */
    Long createReview(Long userId, GameReviewRequest gameReviewRequest);

    /**
     * 获取游戏的所有评价
     *
     * @param gameId 游戏ID
     * @return 评价列表
     */
    List<GameReviewVO> getGameReviews(Long gameId);

    /**
     * 分页获取游戏评价
     *
     * @param gameId   游戏ID
     * @param current  当前页
     * @param pageSize 每页大小
     * @return 分页评价列表
     */
    Page<GameReviewVO> pageGameReviews(Long gameId, long current, long pageSize);

    /**
     * 删除评价
     *
     * @param reviewId 评价ID
     * @param userId   用户ID
     * @return 是否删除成功
     */
    boolean deleteReview(Long reviewId, Long userId);

    /**
     * 获取游戏的平均评分
     *
     * @param gameId 游戏ID
     * @return 平均评分
     */
    Double getGameAverageRating(Long gameId);

    /**
     * 修改评价
     *
     * @param userId                  用户ID
     * @param gameReviewUpdateRequest 评价更新请求
     * @return 是否修改成功
     */
    boolean updateReview(Long userId, GameReviewUpdateRequest gameReviewUpdateRequest);
} 