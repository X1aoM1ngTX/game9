package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.UserLibrary;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【userLibrary(用户游戏库)】的数据库操作Service
 * @创建时间 2024-11-13 15:11:22
 */
public interface UserLibraryService extends IService<UserLibrary> {

    /**
     * 添加游戏到用户游戏库
     *
     * @param userId 用户id
     * @param gameId 游戏id
     * @return 是否添加成功
     */
    boolean addUserGame(Long userId, Long gameId);

    /**
     * 从用户游戏库移除游戏
     *
     * @param userId 用户id
     * @param gameId 游戏id
     * @return 是否移除成功
     */
    boolean removeUserGame(Long userId, Long gameId);

    /**
     * 获取用户的游戏库列表
     *
     * @param userId 用户id
     * @return 游戏列表
     */
    List<Game> getUserGames(Long userId);

    /**
     * 检查用户是否拥有某游戏
     *
     * @param userId 用户ID
     * @param gameId 游戏ID
     * @return 是否拥有
     */
    boolean hasGame(Long userId, Long gameId);
}
