package com.xm.xmgame.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.model.request.game.GameCreateRequest;
import com.xm.xmgame.model.request.game.GameQueryRequest;
import com.xm.xmgame.model.request.game.GameStatusRequest;
import com.xm.xmgame.model.request.game.GameUpdateRequest;
import com.xm.xmgame.service.GameService;
import com.xm.xmgame.mapper.GameMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author XMTX8yyds
 * @description 针对表【game(游戏表)】的数据库操作Service实现
 * @createDate 2024-11-11 14:15:44
 */
@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game> implements GameService {

    @Resource
    private GameMapper gameMapper;

    /**
     *
     * @param gameCreateRequest 创建游戏请求
     * @return Long 游戏ID
     */
    @Override
    public Long createGame(GameCreateRequest gameCreateRequest) {
        if (gameCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        String gameName = gameCreateRequest.getGameName();
        BigDecimal gamePrice = gameCreateRequest.getGamePrice();
        Integer gameStock = gameCreateRequest.getGameStock();

        // 参数校验
        if (StringUtils.isBlank(gameName) || gamePrice == null || gameStock == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (gamePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游戏价格不能为负");
        }
        if (gameStock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游戏库存不能为负");
        }

        // 检查游戏名是否重复
        long count = lambdaQuery().eq(Game::getGameName, gameName).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.GAME_EXIST, "游戏名称已存在");
        }

        Game game = new Game();
        game.setGameName(gameName);
        game.setGamePrice(gamePrice);
        game.setGameStock(gameStock);

        boolean saveResult = save(game);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建失败");
        }
        return game.getGameId();
    }

    /**
     *
     * @return
     */
    @Override
    public List<Game> getAllGames() {
        return lambdaQuery()
                .orderByDesc(Game::getGameCreatedTime)
                .list();
    }

    /**
     *
     * @param gameQueryRequest 查询条件
     * @return
     */
    @Override
    public Page<Game> pageGames(GameQueryRequest gameQueryRequest) {
        if (gameQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        LambdaQueryWrapper<Game> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(gameQueryRequest.getGameName()),
                         Game::getGameName, gameQueryRequest.getGameName())
                   .eq(gameQueryRequest.getShowAvailableOnly() != null && gameQueryRequest.getShowAvailableOnly(),
                       Game::getGameIsRemoved, false)
                   .orderByDesc(Game::getGameCreatedTime);

        return page(new Page<>(gameQueryRequest.getCurrent(), gameQueryRequest.getPageSize()),
                   queryWrapper);
    }

    /**
     *
     * @param gameUpdateRequest 更新游戏请求
     * @return
     */
    @Override
    public boolean updateGame(GameUpdateRequest gameUpdateRequest) {
        if (gameUpdateRequest == null || gameUpdateRequest.getGameId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Game game = getById(gameUpdateRequest.getGameId());
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 更新游戏信息
        game.setGameName(gameUpdateRequest.getGameName());
        game.setGameDescription(gameUpdateRequest.getGameDescription());
        game.setGamePrice(gameUpdateRequest.getGamePrice());
        game.setGameStock(gameUpdateRequest.getGameStock());
        game.setGamePub(gameUpdateRequest.getGamePub());
        game.setGameDev(gameUpdateRequest.getGameDev());
        game.setGameReleaseDate(gameUpdateRequest.getGameReleaseDate());
        game.setGameIsRemoved(gameUpdateRequest.isGameIsRemoved());

        return updateById(game);
    }

    /**
     *
     * @param gameStatusRequest 游戏状态请求
     * @return
     */
    @Override
    public boolean setGameRemovedStatus(GameStatusRequest gameStatusRequest) {
        if (gameStatusRequest == null || gameStatusRequest.getGameId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Game game = getById(gameStatusRequest.getGameId());
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        game.setGameIsRemoved(gameStatusRequest.isGameIsRemoved());
        return updateById(game);
    }

    /**
     *
     * @param gameId 游戏id
     * @return
     */
    @Override
    public boolean deleteGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return removeById(gameId);
    }
}




