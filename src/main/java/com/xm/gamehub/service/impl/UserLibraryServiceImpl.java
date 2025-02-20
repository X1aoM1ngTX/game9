package com.xm.gamehub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.gamehub.common.ErrorCode;
import com.xm.gamehub.exception.BusinessException;
import com.xm.gamehub.model.domain.Game;
import com.xm.gamehub.model.domain.UserLibrary;
import com.xm.gamehub.service.UserLibraryService;
import com.xm.gamehub.mapper.UserLibraryMapper;
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        
        // 检查是否已拥有该游戏
        if (hasGame(userId, gameId)) {
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        
        // 检查是否拥有该游戏
        if (!hasGame(userId, gameId)) {
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return userLibraryMapper.selectGamesByUserId(userId);
    }

    /**
     * 检查用户是否拥有某游戏
     * 
     * @param userId 用户ID
     * @param gameId 游戏ID
     * @return 是否拥有
     */
    @Override
    public boolean hasGame(Long userId, Long gameId) {
        if (userId == null || userId <= 0 || gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        
        return lambdaQuery()
                .eq(UserLibrary::getUserId, userId)
                .eq(UserLibrary::getGameId, gameId)
                .count() > 0;
    }
}




