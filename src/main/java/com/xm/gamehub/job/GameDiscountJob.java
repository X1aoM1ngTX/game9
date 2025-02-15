package com.xm.gamehub.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.gamehub.model.domain.Game;
import com.xm.gamehub.service.GameService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 游戏折扣定时任务
 */
@Component
@Slf4j
public class GameDiscountJob {

    @Resource
    private GameService gameService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 每秒检查一次折扣是否过期
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkDiscountExpiration() {
        log.info("开始检查游戏折扣过期情况");

        // 先查询所有过期的游戏，用于日志记录
        String checkSql = """
                SELECT gameId, gameName, gameSaleEndTime
                FROM game
                WHERE gameOnSale = 1
                AND gameSaleEndTime < NOW()
                """;

        List<Game> expiredGames = jdbcTemplate.query(checkSql, (rs, rowNum) -> {
            Game game = new Game();
            game.setGameId(rs.getLong("gameId"));
            game.setGameName(rs.getString("gameName"));
            game.setGameSaleEndTime(rs.getTimestamp("gameSaleEndTime"));
            return game;
        });

        log.info("发现 {} 个过期折扣游戏", expiredGames.size());
        for (Game game : expiredGames) {
            log.info("游戏 {} (ID: {}) 的折扣已过期，过期时间: {}",
                    game.getGameName(), game.getGameId(), game.getGameSaleEndTime());
        }

        // 更新过期的折扣
        String updateSql = """
                    UPDATE game
                    SET gameOnSale = 0,
                        gameDiscountedPrices = NULL,
                        gameDiscount = NULL,
                        gameSaleStartTime = NULL,
                        gameSaleEndTime = NULL
                    WHERE gameOnSale = 1 AND gameSaleEndTime < NOW()
                """;

        int updatedCount = jdbcTemplate.update(updateSql);
        log.info("实际更新了 {} 个游戏的折扣状态", updatedCount);

        if (updatedCount > 0) {
            // 验证更新结果
            String verifySql = """
                        SELECT COUNT(*)
                        FROM game
                        WHERE gameOnSale = 1
                        AND gameSaleEndTime < NOW()
                    """;

            @SuppressWarnings("null")
            int remainingCount = jdbcTemplate.queryForObject(verifySql, Integer.class);
            log.info("更新后仍有 {} 个过期未处理的游戏", remainingCount);

            // 获取更新后的游戏列表
            LambdaQueryWrapper<Game> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Game::getGameOnSale, 0)
                    .isNull(Game::getGameDiscount)
                    .isNull(Game::getGameDiscountedPrices);

            List<Game> updatedGames = gameService.list(queryWrapper);

            for (Game game : updatedGames) {
                log.info("游戏 {} 的折扣已被清除", game.getGameName());
            }

            if (updatedCount != expiredGames.size()) {
                log.warn("预期更新 {} 个游戏，实际更新了 {} 个游戏",
                        expiredGames.size(), updatedCount);
            }
        } else {
            log.info("没有发现需要更新的过期折扣");
        }
    }
} 