package com.xm.xmgame.service;

import com.xm.xmgame.model.domain.Game;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.xmgame.model.request.game.GameCreateRequest;
import com.xm.xmgame.model.request.game.GameQueryRequest;
import com.xm.xmgame.model.request.game.GameStatusRequest;
import com.xm.xmgame.model.request.game.GameUpdateRequest;
import com.xm.xmgame.model.vo.GameDetailVO;

import java.util.List;

/**
 * @author XMTX8yyds
 * @description 针对表【game(游戏表)】的数据库操作Service
 * @createDate 2024-11-11 14:15:44
 */
public interface GameService extends IService<Game> {

    /**
     * 创建游戏
     *
     * @param gameCreateRequest 创建游戏请求
     * @return gameId
     */
    Long createGame(GameCreateRequest gameCreateRequest);

    /**
     * 获取所有游戏
     *
     * @return AllGames
     */
    List<Game> getAllGames();

    /**
     * 分页获取游戏列表
     *
     * @param gameQueryRequest 查询条件
     * @return 分页游戏列表
     */
    Page<Game> pageGames(GameQueryRequest gameQueryRequest);

    /**
     * 获取游戏详情
     *
     * @param gameId 游戏id
     * @return 游戏详情
     */
    GameDetailVO getGameDetail(Long gameId);

    /**
     * 更新游戏
     *
     * @param gameUpdateRequest 更新游戏请求
     * @return boolean
     */
    boolean updateGame(GameUpdateRequest gameUpdateRequest);

    /**
     * 设置游戏是否下架
     *
     * @param gameStatusRequest 游戏状态请求
     * @return boolean
     */
    boolean setGameRemovedStatus(GameStatusRequest gameStatusRequest);

    /**
     * 删除游戏
     *
     * @param gameId 游戏id
     * @return boolean
     */
    boolean deleteGame(Long gameId);

}
