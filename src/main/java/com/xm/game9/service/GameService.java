package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.request.admin.BatchImportGamesRequest;
import com.xm.game9.model.request.game.GameCreateRequest;
import com.xm.game9.model.request.game.GameQueryRequest;
import com.xm.game9.model.request.game.GameStatusRequest;
import com.xm.game9.model.request.game.GameSteamUrlUpdateRequest;
import com.xm.game9.model.request.game.GameUpdateRequest;
import com.xm.game9.model.vo.GameDetailVO;
import com.xm.game9.model.vo.order.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @description 针对表【game(游戏表)】的数据库操作Service
 * @createDate 2024-11-11 14:15:44
 */
@Service
@Transactional
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

    /**
     * 购买游戏 - 创建订单
     *
     * @param userId 用户ID
     * @param gameId 游戏ID
     * @return 订单信息
     */
    OrderVO createPurchaseOrder(Long userId, Long gameId);

    /**
     * 购买游戏（旧方法，保留兼容性）
     *
     * @param userId 用户ID
     * @param gameId 游戏ID
     * @return 是否购买成功
     */
    @Deprecated
    boolean purchaseGame(Long userId, Long gameId);

    /**
     * 批量导入游戏
     */
    int batchImportGames(List<BatchImportGamesRequest.GameImportInfo> games);

    /**
     * 通过Steam URL更新游戏AppID
     *
     * @param gameSteamUrlUpdateRequest Steam URL更新请求
     * @return 是否更新成功
     */
    boolean updateGameAppIdBySteamUrl(GameSteamUrlUpdateRequest gameSteamUrlUpdateRequest);

}
