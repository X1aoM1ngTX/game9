package com.xm.xmgame.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.model.domain.UserLibrary;
import com.xm.xmgame.service.GameService;
import com.xm.xmgame.service.UserLibraryService;
import com.xm.xmgame.mapper.UserLibraryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【userLibrary(用户游戏库)】的数据库操作Service实现
 * @创建时间 2024-11-13 15:11:22
 */
@Service
@Slf4j
public class UserLibraryServiceImpl extends ServiceImpl<UserLibraryMapper, UserLibrary> implements UserLibraryService {

    @Autowired
    private UserLibraryMapper userLibraryMapper;

    @Autowired
    private GameService gameService;

    /**
     * 添加游戏到用户游戏库
     *
     * @param userId 用户id
     * @param gameId 游戏id
     * @return 是否添加成功
     */
    @Override
    public boolean addUserGame(Long userId, Long gameId) {
        if (userId == null || userId <= 0 || gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 检查游戏是否存在
        Game game = gameService.getById(gameId);
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }
        
        // 检查游戏是否已下架
        if (game.getGameIsRemoved()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游戏已下架");
        }
        
        // 检查是否已拥有该游戏
        Long count = lambdaQuery()
                .eq(UserLibrary::getUserId, userId)
                .eq(UserLibrary::getGameId, gameId)
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已拥有该游戏");
        }

        // 添加游戏到用户游戏库
        UserLibrary userLibrary = new UserLibrary();
        userLibrary.setUserId(userId);
        userLibrary.setGameId(gameId);
        return save(userLibrary);
    }

    /**
     * 从用户游戏库移除游戏
     *
     * @param userId 用户id
     * @param gameId 游戏id
     * @return 是否移除成功
     */
    @Override
    public boolean removeUserGame(Long userId, Long gameId) {
        if (userId == null || userId <= 0 || gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 检查是否拥有该游戏
        Long count = lambdaQuery()
                .eq(UserLibrary::getUserId, userId)
                .eq(UserLibrary::getGameId, gameId)
                .count();
        if (count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未拥有该游戏");
        }

        return remove(new LambdaQueryWrapper<UserLibrary>()
                .eq(UserLibrary::getUserId, userId)
                .eq(UserLibrary::getGameId, gameId));
    }

    /**
     * 获取用户的游戏库列表
     *
     * @param userId 用户id
     * @return 游戏列表
     */
    @Override
    public List<Game> getUserGames(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userLibraryMapper.selectGamesByUserId(userId);
    }
}




