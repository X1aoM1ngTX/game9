package com.xm.game9.service.impl;

import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.GameReviewMapper;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.GameReview;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.game.GameReviewRequest;
import com.xm.game9.model.request.game.GameReviewUpdateRequest;
import com.xm.game9.service.GameService;
import com.xm.game9.service.UserLibraryService;
import com.xm.game9.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameReviewServiceImplTest {

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @Mock
    private UserLibraryService userLibraryService;

    @Mock
    private GameReviewMapper gameReviewMapper;

    @InjectMocks
    private GameReviewServiceImpl gameReviewService;

    private GameReviewRequest reviewRequest;
    private GameReviewUpdateRequest updateRequest;
    private Game testGame;
    private User testUser;
    private GameReview testReview;

    @BeforeEach
    void setUp() {
        // 创建测试游戏
        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setGameName("Test Game");

        // 创建测试用户
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testuser");

        // 创建测试评价请求
        reviewRequest = new GameReviewRequest();
        reviewRequest.setGameId(1L);
        reviewRequest.setRating(5);
        reviewRequest.setContent("This is a great game!");

        // 创建测试评价更新请求
        updateRequest = new GameReviewUpdateRequest();
        updateRequest.setReviewId(1L);
        updateRequest.setRating(4);
        updateRequest.setContent("Updated review content");

        // 创建测试评价
        testReview = new GameReview();
        testReview.setReviewId(1L);
        testReview.setUserId(1L);
        testReview.setGameId(1L);
        testReview.setGameReviewRating(5);
        testReview.setGameReviewContent("Great game!");
        testReview.setGameReviewCreateTime(new Date());
        testReview.setGameReviewUpdateTime(new Date());
        testReview.setGameReviewIsDeleted(0);
    }

    @Test
    void createReview_Success() {
        Long userId = 1L;

        // 模拟用户存在
        when(userService.getById(userId)).thenReturn(testUser);
        
        // 模拟游戏存在
        when(gameService.lambdaQuery().eq(Game::getGameId, 1L).exists()).thenReturn(true);
        
        // 模拟用户拥有游戏
        when(userLibraryService.lambdaQuery()
                .eq(any(), eq(userId))
                .eq(any(), eq(1L))
                .exists()).thenReturn(true);
        
        // 模拟用户未评价过
        when(gameReviewService.lambdaQuery()
                .eq(any(), eq(userId))
                .eq(any(), eq(1L))
                .eq(any(), eq(false))
                .exists()).thenReturn(false);
        
        // 模拟保存成功
        when(gameReviewMapper.insert(any(GameReview.class))).thenReturn(1);

        Long reviewId = gameReviewService.createReview(userId, reviewRequest);

        assertNotNull(reviewId);
        verify(gameReviewMapper).insert(any(GameReview.class));
    }

    @Test
    void createReview_NullParameters_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.createReview(null, null);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("参数错误", exception.getMessage());
    }

    @Test
    void createReview_UserNotFound_ThrowsException() {
        Long userId = 999L;

        when(userService.getById(userId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.createReview(userId, reviewRequest);
        });
        
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getErrorCode(), exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void createReview_GameNotFound_ThrowsException() {
        Long userId = 1L;

        when(userService.getById(userId)).thenReturn(testUser);
        when(gameService.lambdaQuery().eq(Game::getGameId, 1L).exists()).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.createReview(userId, reviewRequest);
        });
        
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getErrorCode(), exception.getCode());
        assertEquals("游戏不存在", exception.getMessage());
    }

    @Test
    void createReview_UserNotOwnGame_ThrowsException() {
        Long userId = 1L;

        when(userService.getById(userId)).thenReturn(testUser);
        when(gameService.lambdaQuery().eq(Game::getGameId, 1L).exists()).thenReturn(true);
        when(userLibraryService.lambdaQuery()
                .eq(any(), eq(userId))
                .eq(any(), eq(1L))
                .exists()).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.createReview(userId, reviewRequest);
        });
        
        assertEquals(ErrorCode.NO_AUTH.getErrorCode(), exception.getCode());
        assertEquals("您需要先购买游戏才能评价", exception.getMessage());
    }

    @Test
    void createReview_InvalidRating_ThrowsException() {
        Long userId = 1L;
        reviewRequest.setRating(0); // 无效评分

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.createReview(userId, reviewRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("评分必须是1-5星的整数", exception.getMessage());
    }

    @Test
    void createReview_AlreadyReviewed_ThrowsException() {
        Long userId = 1L;

        when(userService.getById(userId)).thenReturn(testUser);
        when(gameService.lambdaQuery().eq(Game::getGameId, 1L).exists()).thenReturn(true);
        when(userLibraryService.lambdaQuery()
                .eq(any(), eq(userId))
                .eq(any(), eq(1L))
                .exists()).thenReturn(true);
        when(gameReviewService.lambdaQuery()
                .eq(any(), eq(userId))
                .eq(any(), eq(1L))
                .eq(any(), eq(false))
                .exists()).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.createReview(userId, reviewRequest);
        });
        
        assertEquals(ErrorCode.OPERATION_ERROR.getErrorCode(), exception.getCode());
        assertEquals("您已经评价过该游戏", exception.getMessage());
    }

    @Test
    void deleteReview_Success() {
        Long reviewId = 1L;
        Long userId = 1L;

        when(gameReviewMapper.selectById(reviewId)).thenReturn(testReview);
        when(gameReviewMapper.updateById(any(GameReview.class))).thenReturn(1);

        boolean result = gameReviewService.deleteReview(reviewId, userId);

        assertTrue(result);
        verify(gameReviewMapper).updateById(any(GameReview.class));
    }

    @Test
    void deleteReview_ReviewNotFound_ThrowsException() {
        Long reviewId = 999L;
        Long userId = 1L;

        when(gameReviewMapper.selectById(reviewId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.deleteReview(reviewId, userId);
        });
        
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getErrorCode(), exception.getCode());
        assertEquals("评价不存在", exception.getMessage());
    }

    @Test
    void deleteReview_UnauthorizedUser_ThrowsException() {
        Long reviewId = 1L;
        Long userId = 2L; // 不同的用户

        when(gameReviewMapper.selectById(reviewId)).thenReturn(testReview);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.deleteReview(reviewId, userId);
        });
        
        assertEquals(ErrorCode.NO_AUTH.getErrorCode(), exception.getCode());
        assertEquals("无权删除该评价", exception.getMessage());
    }

    @Test
    void updateReview_Success() {
        Long userId = 1L;

        when(gameReviewMapper.selectById(1L)).thenReturn(testReview);
        when(gameReviewMapper.updateById(any(GameReview.class))).thenReturn(1);

        boolean result = gameReviewService.updateReview(userId, updateRequest);

        assertTrue(result);
        verify(gameReviewMapper).updateById(any(GameReview.class));
    }

    @Test
    void updateReview_ReviewNotFound_ThrowsException() {
        Long userId = 1L;

        when(gameReviewMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.updateReview(userId, updateRequest);
        });
        
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getErrorCode(), exception.getCode());
        assertEquals("评价不存在", exception.getMessage());
    }

    @Test
    void updateReview_UnauthorizedUser_ThrowsException() {
        Long userId = 2L; // 不同的用户

        when(gameReviewMapper.selectById(1L)).thenReturn(testReview);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.updateReview(userId, updateRequest);
        });
        
        assertEquals(ErrorCode.NO_AUTH.getErrorCode(), exception.getCode());
        assertEquals("无权修改该评价", exception.getMessage());
    }

    @Test
    void updateReview_InvalidRating_ThrowsException() {
        Long userId = 1L;
        updateRequest.setRating(6); // 无效评分

        when(gameReviewMapper.selectById(1L)).thenReturn(testReview);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            gameReviewService.updateReview(userId, updateRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("评分必须是1-5星的整数", exception.getMessage());
    }
}