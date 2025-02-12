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
import com.xm.xmgame.model.vo.GameDetailVO;
import com.xm.xmgame.service.GameService;
import com.xm.xmgame.mapper.GameMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * @author xm
 * @description 针对表【game(游戏表)】的数据库操作Service实现
 * @createDate 2024-11-11 14:15:44
 */
@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game> implements GameService {

    @Resource
    private GameMapper gameMapper;

    /**
     * 创建游戏
     *
     * @param gameCreateRequest 创建游戏请求
     * @return Long 游戏ID
     */
    @Override
    public Long createGame(GameCreateRequest gameCreateRequest) {
        if (gameCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
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
     * 查询所有游戏
     *
     * @return 所有游戏
     */
    @Override
    public List<Game> getAllGames() {
        return lambdaQuery()
                .orderByDesc(Game::getGameCreatedTime)
                .list();
    }

    /**
     * 查询游戏
     *
     * @param gameQueryRequest 游戏查询请求
     * @return 游戏查询结果
     */
    @Override
    public Page<Game> pageGames(GameQueryRequest gameQueryRequest) {
        if (gameQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
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
     * 更新游戏状态
     *
     * @param gameId 游戏id
     * @return boolean (是否更新成功)
     */
    @Override
    public GameDetailVO getGameDetail(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误");
        }

        // 获取游戏基本信息
        Game game = getById(gameId);
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        // 转换为VO对象
        GameDetailVO gameDetailVO = new GameDetailVO();
        gameDetailVO.setGameId(game.getGameId());
        gameDetailVO.setGameName(game.getGameName());
        gameDetailVO.setGameDescription(game.getGameDescription());
        gameDetailVO.setGamePrice(game.getGamePrice());
        gameDetailVO.setGameStock(game.getGameStock());
        gameDetailVO.setGameReleaseDate(game.getGameReleaseDate());
        gameDetailVO.setGameDev(game.getGameDev());
        gameDetailVO.setGamePub(game.getGamePub());
        gameDetailVO.setGameCover(game.getGameCover());
        gameDetailVO.setGameIsRemoved(game.getGameIsRemoved());

        // 处理折扣信息
        if (game.getGameOnSale() == 1 && game.getGameSaleEndTime() != null
                && game.getGameSaleEndTime().after(new Date())) {
            gameDetailVO.setGameOnSale(1);
            gameDetailVO.setGameDiscount(game.getGameDiscount());
            gameDetailVO.setGameSaleEndTime(game.getGameSaleEndTime());
            gameDetailVO.setGameDiscountedPrices(game.getGameDiscountedPrices());
        } else {
            gameDetailVO.setGameOnSale(0);
            gameDetailVO.setGameDiscountedPrices(game.getGamePrice());
        }

        return gameDetailVO;
    }


    /**
     * 更新游戏
     *
     * @param gameUpdateRequest 游戏更新请求
     * @return boolean (是否更新成功)
     */
    @Override
    public boolean updateGame(GameUpdateRequest gameUpdateRequest) {
        if (gameUpdateRequest == null || gameUpdateRequest.getGameId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        Game game = getById(gameUpdateRequest.getGameId());
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        // 更新基本信息
        game.setGameName(gameUpdateRequest.getGameName());
        game.setGameDescription(gameUpdateRequest.getGameDescription());
        if (gameUpdateRequest.getGamePrice() != null) {
            try {
                game.setGamePrice(new BigDecimal(String.valueOf(gameUpdateRequest.getGamePrice())));
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格格式不正确");
            }
        }
        if (gameUpdateRequest.getGamePrice() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格不能为空");
        }
        if (new BigDecimal(gameUpdateRequest.getGamePrice()).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格不能为负数");
        }
        // 检查免费游戏不能打折
        boolean GamePriceIsFree = false;
        boolean GameOnSale = false;
        if (game.getGamePrice().compareTo(BigDecimal.ZERO) < 0) {
            GamePriceIsFree = true;
        }
        if (game.getGameOnSale() == 1) {
            GameOnSale = true;
        }
        if (GamePriceIsFree && GameOnSale) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "免费游戏不能打折");
        }
        game.setGameStock(gameUpdateRequest.getGameStock());
        game.setGamePub(gameUpdateRequest.getGamePub());
        game.setGameDev(gameUpdateRequest.getGameDev());
        game.setGameReleaseDate(gameUpdateRequest.getGameReleaseDate());
        game.setGameOnSale(gameUpdateRequest.getGameOnSale() ? 1 : 0);
        if (gameUpdateRequest.getGameOnSale()) {
            game.setGameDiscount(gameUpdateRequest.getGameDiscount());
            game.setGameSaleStartTime(gameUpdateRequest.getGameSaleStartTime());
            game.setGameSaleEndTime(gameUpdateRequest.getGameSaleEndTime());
            // 计算折扣价格
            if (game.getGamePrice() != null && gameUpdateRequest.getGameDiscount() != null) {
                BigDecimal discountedPrice = game.getGamePrice()
                        .multiply(BigDecimal.ONE.subtract(gameUpdateRequest.getGameDiscount()))
                        .setScale(2, RoundingMode.HALF_UP);
                game.setGameDiscountedPrices(discountedPrice);
            }
        } else {
            // 关闭折扣时，清空所有折扣相关信息
            game.setGameDiscount(BigDecimal.ZERO);
            game.setGameSaleStartTime(null);
            game.setGameSaleEndTime(null);
            game.setGameDiscountedPrices(null);
        }
        game.setGameCover(gameUpdateRequest.getGameCover());
        return updateById(game);
    }

    /**
     * 设置游戏状态
     *
     * @param gameStatusRequest 游戏状态请求
     * @return boolean (是否设置成功)
     */
    @Override
    public boolean setGameRemovedStatus(GameStatusRequest gameStatusRequest) {
        if (gameStatusRequest == null || gameStatusRequest.getGameId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        Game game = getById(gameStatusRequest.getGameId());
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        game.setGameIsRemoved(gameStatusRequest.isGameIsRemoved());
        return updateById(game);
    }

    /**
     * 删除游戏(物理删除)
     *
     * @param gameId 游戏id
     * @return 是否删除成功
     */
    @Override
    public boolean deleteGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return removeById(gameId);
    }

}




