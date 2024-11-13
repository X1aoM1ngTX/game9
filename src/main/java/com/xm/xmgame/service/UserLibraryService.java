package com.xm.xmgame.service;

import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.model.domain.UserLibrary;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author XMTX8yyds
 * @描述 针对表【userLibrary(用户游戏库)】的数据库操作Service
 * @创建时间 2024-11-13 15:11:22
 */
public interface UserLibraryService extends IService<UserLibrary> {

    /**
     *
     * @param userId
     * @param gameId
     * @return
     */
    boolean addUserGame(Long userId, Long gameId);

    /**
     *
     * @param userId
     * @param gameId
     * @return
     */
    boolean removeUserGame(Long userId, Long gameId);

    /**
     *
     * @param userId
     * @return
     */
    List<Game> getUserGames(Long userId);
}
