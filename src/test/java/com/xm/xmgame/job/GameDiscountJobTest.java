package com.xm.xmgame.job;

import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.service.GameService;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.math.BigDecimal;
import java.util.Date;

@SpringBootTest
class GameDiscountJobTest {

    @Resource
    private GameService gameService;

    @Resource
    private GameDiscountJob gameDiscountJob;

    @Test
    void testCheckDiscountExpiration() {
        // 1. 创建一个测试游戏
        Game game = new Game();
        game.setGameName("折扣测试游戏");
        game.setGamePrice(new BigDecimal("100"));
        game.setGameStock(100);
        game.setGameIsRemoved(false);
        gameService.save(game);

        // 2. 设置已过期的折扣
        game.setGameOnSale(1);
        game.setGameDiscount(new BigDecimal("0.2")); // 8折
        game.setGameDiscountedPrices(new BigDecimal("80"));
        
        // 设置过期时间为5分钟前
        Date now = new Date();
        Date startTime = new Date(now.getTime() - 10 * 60 * 1000); // 10分钟前
        Date endTime = new Date(now.getTime() - 5 * 60 * 1000);    // 5分钟前
        
        game.setGameSaleStartTime(startTime);
        game.setGameSaleEndTime(endTime);
        boolean updateResult = gameService.updateById(game);
        Assertions.assertTrue(updateResult, "更新游戏折扣信息失败");

        // 3. 执行定时任务
        gameDiscountJob.checkDiscountExpiration();

        // 4. 验证结果
        Game updatedGame = gameService.getById(game.getGameId());
        Assertions.assertNotNull(updatedGame, "获取更新后的游戏信息失败");
        
        // 使用更详细的断言
        Assertions.assertEquals(0, updatedGame.getGameOnSale(), 
                "游戏折扣状态未正确清除，当前状态: " + updatedGame.getGameOnSale());
        Assertions.assertNull(updatedGame.getGameDiscount(), 
                "游戏折扣未正确清除，当前折扣: " + updatedGame.getGameDiscount());
        Assertions.assertNull(updatedGame.getGameDiscountedPrices(), 
                "游戏折扣价格未正确清除，当前价格: " + updatedGame.getGameDiscountedPrices());
        Assertions.assertNull(updatedGame.getGameSaleStartTime(), 
                "游戏折扣开始时间未正确清除，当前时间: " + updatedGame.getGameSaleStartTime());
        Assertions.assertNull(updatedGame.getGameSaleEndTime(), 
                "游戏折扣结束时间未正确清除，当前时间: " + updatedGame.getGameSaleEndTime());

        // 5. 清理测试数据
        gameService.removeById(game.getGameId());
    }

    @Test
    void testNotExpiredDiscount() {
        // 1. 创建一个测试游戏
        Game game = new Game();
        game.setGameName("未过期折扣测试游戏");
        game.setGamePrice(new BigDecimal("100"));
        game.setGameStock(100);
        game.setGameIsRemoved(false);
        gameService.save(game);

        // 2. 设置未过期的折扣
        game.setGameOnSale(1);
        game.setGameDiscount(new BigDecimal("0.2")); // 8折
        game.setGameDiscountedPrices(new BigDecimal("80"));
        
        // 设置未来的结束时间
        Date now = new Date();
        Date startTime = new Date(now.getTime() - 5 * 60 * 1000);  // 5分钟前
        Date endTime = new Date(now.getTime() + 5 * 60 * 1000);    // 5分钟后
        
        game.setGameSaleStartTime(startTime);
        game.setGameSaleEndTime(endTime);
        boolean updateResult = gameService.updateById(game);
        Assertions.assertTrue(updateResult, "更新游戏折扣信息失败");

        // 3. 执行定时任务
        gameDiscountJob.checkDiscountExpiration();

        // 4. 验证结果 - 折扣应该保持不变
        Game updatedGame = gameService.getById(game.getGameId());
        Assertions.assertNotNull(updatedGame, "获取更新后的游戏信息失败");
        
        Assertions.assertEquals(1, updatedGame.getGameOnSale(), 
                "游戏折扣状态发生变化，当前状态: " + updatedGame.getGameOnSale());
        Assertions.assertEquals(0, updatedGame.getGameDiscount().compareTo(new BigDecimal("0.2")), 
                "游戏折扣发生变化，当前折扣: " + updatedGame.getGameDiscount());
        Assertions.assertEquals(0, updatedGame.getGameDiscountedPrices().compareTo(new BigDecimal("80")), 
                "游戏折扣价格发生变化，当前价格: " + updatedGame.getGameDiscountedPrices());
        Assertions.assertNotNull(updatedGame.getGameSaleStartTime(), 
                "游戏折扣开始时间被清除");
        Assertions.assertNotNull(updatedGame.getGameSaleEndTime(), 
                "游戏折扣结束时间被清除");

        // 5. 清理测试数据
        gameService.removeById(game.getGameId());
    }

    @Autowired
    JavaMailSender javaMailSender;

    @Test
    public void test() throws Exception {

        // 创建一个邮件消息
        MimeMessage message = javaMailSender.createMimeMessage();

        // 创建 MimeMessageHelper
        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        // 发件人邮箱和名称
        helper.setFrom("noneedtofan@qq.com", "springdoc");
        // 收件人邮箱
        helper.setTo("1062829664@qq.com");
        // 邮件标题
        helper.setSubject("Hello World.");
        // 邮件正文，第二个参数表示是否是HTML正文
        helper.setText("Hello <strong> World</strong>！", true);

        // 发送
        javaMailSender.send(message);
    }
} 