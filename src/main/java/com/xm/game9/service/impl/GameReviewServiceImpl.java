package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.GameReviewMapper;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.GameReview;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.domain.UserLibrary;
import com.xm.game9.model.request.game.GameReviewRequest;
import com.xm.game9.model.request.game.GameReviewUpdateRequest;
import com.xm.game9.model.vo.GameReviewVO;
import com.xm.game9.service.GameReviewService;
import com.xm.game9.service.GameService;
import com.xm.game9.service.UserLibraryService;
import com.xm.game9.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author X1aoM1ngTX
 * @description 针对表【gameReview(游戏评价表)】的数据库操作Service实现
 * @createDate 2024-03-19
 */
@Service
@Slf4j
public class GameReviewServiceImpl extends ServiceImpl<GameReviewMapper, GameReview> implements GameReviewService {

    @Resource
    private GameService gameService;

    @Resource
    private UserService userService;

    @Resource
    private UserLibraryService userLibraryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReview(Long userId, GameReviewRequest gameReviewRequest) {
        if (userId == null || gameReviewRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 检查游戏是否存在
        if (!gameService.lambdaQuery().eq(Game::getGameId, gameReviewRequest.getGameId()).exists()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        // 检查用户是否拥有该游戏
        if (!userLibraryService.lambdaQuery()
                .eq(UserLibrary::getUserId, userId)
                .eq(UserLibrary::getGameId, gameReviewRequest.getGameId())
                .exists()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "您需要先购买游戏才能评价");
        }

        // 检查评分是否在有效范围内
        if (gameReviewRequest.getRating().compareTo(new BigDecimal("1")) < 0 || 
            gameReviewRequest.getRating().compareTo(new BigDecimal("5")) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");
        }

        // 检查评分是否为0.5的倍数
        if (gameReviewRequest.getRating().remainder(new BigDecimal("0.5")).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须是0.5的倍数");
        }

        // 检查是否已经评价过
        if (lambdaQuery()
                .eq(GameReview::getUserId, userId)
                .eq(GameReview::getGameId, gameReviewRequest.getGameId())
                .eq(GameReview::getIsDeleted, false)
                .exists()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经评价过该游戏");
        }

        GameReview gameReview = new GameReview();
        gameReview.setUserId(userId);
        gameReview.setGameId(gameReviewRequest.getGameId());
        gameReview.setRating(gameReviewRequest.getRating());
        gameReview.setContent(gameReviewRequest.getContent());
        gameReview.setCreateTime(new Date());
        gameReview.setUpdateTime(new Date());
        gameReview.setIsDeleted(false);

        boolean result = save(gameReview);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评价失败");
        }

        return gameReview.getReviewId();
    }

    @Override
    public List<GameReviewVO> getGameReviews(Long gameId) {
        if (gameId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        List<GameReview> gameReviews = lambdaQuery()
                .eq(GameReview::getGameId, gameId)
                .eq(GameReview::getIsDeleted, false)
                .orderByDesc(GameReview::getCreateTime)
                .list();

        return convertToVOList(gameReviews);
    }

    @Override
    public Page<GameReviewVO> pageGameReviews(Long gameId, long current, long pageSize) {
        if (gameId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        Page<GameReview> gameReviewPage = lambdaQuery()
                .eq(GameReview::getGameId, gameId)
                .eq(GameReview::getIsDeleted, false)
                .orderByDesc(GameReview::getCreateTime)
                .page(new Page<>(current, pageSize));

        Page<GameReviewVO> gameReviewVOPage = new Page<>(gameReviewPage.getCurrent(), gameReviewPage.getSize(), gameReviewPage.getTotal());
        gameReviewVOPage.setRecords(convertToVOList(gameReviewPage.getRecords()));

        return gameReviewVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReview(Long reviewId, Long userId) {
        if (reviewId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        GameReview gameReview = getById(reviewId);
        if (gameReview == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }

        // 检查是否是评价的作者
        if (!gameReview.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权删除他人的评价");
        }

        return lambdaUpdate()
                .eq(GameReview::getReviewId, reviewId)
                .set(GameReview::getIsDeleted, true)
                .set(GameReview::getUpdateTime, new Date())
                .update();
    }

    @Override
    public Double getGameAverageRating(Long gameId) {
        if (gameId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        List<GameReview> reviews = lambdaQuery()
                .eq(GameReview::getGameId, gameId)
                .eq(GameReview::getIsDeleted, false)
                .list();

        if (reviews.isEmpty()) {
            return 0.0;
        }

        BigDecimal sum = reviews.stream()
                .map(GameReview::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(reviews.size()), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReview(Long userId, GameReviewUpdateRequest gameReviewUpdateRequest) {
        if (userId == null || gameReviewUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查评价是否存在
        GameReview gameReview = getById(gameReviewUpdateRequest.getReviewId());
        if (gameReview == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }

        // 检查是否是评价的作者
        if (!gameReview.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权修改他人的评价");
        }

        // 检查评分是否在有效范围内
        if (gameReviewUpdateRequest.getRating() != null) {
            if (gameReviewUpdateRequest.getRating().compareTo(new BigDecimal("1")) < 0 || 
                gameReviewUpdateRequest.getRating().compareTo(new BigDecimal("5")) > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");
            }
            
            // 检查评分是否为0.5的倍数
            if (gameReviewUpdateRequest.getRating().remainder(new BigDecimal("0.5")).compareTo(BigDecimal.ZERO) != 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须是0.5的倍数");
            }
        }

        // 更新评价
        return lambdaUpdate()
                .eq(GameReview::getReviewId, gameReviewUpdateRequest.getReviewId())
                .set(gameReviewUpdateRequest.getRating() != null, GameReview::getRating, gameReviewUpdateRequest.getRating())
                .set(StringUtils.isNotBlank(gameReviewUpdateRequest.getContent()), GameReview::getContent, gameReviewUpdateRequest.getContent())
                .set(GameReview::getUpdateTime, new Date())
                .update();
    }

    /**
     * 将GameReview列表转换为GameReviewVO列表
     */
    private List<GameReviewVO> convertToVOList(List<GameReview> gameReviews) {
        return gameReviews.stream().map(gameReview -> {
            GameReviewVO gameReviewVO = new GameReviewVO();
            BeanUtils.copyProperties(gameReview, gameReviewVO);
            
            // 获取用户名
            User user = userService.getById(gameReview.getUserId());
            if (user != null) {
                gameReviewVO.setUserName(user.getUserName());
            }
            
            return gameReviewVO;
        }).collect(Collectors.toList());
    }
} 