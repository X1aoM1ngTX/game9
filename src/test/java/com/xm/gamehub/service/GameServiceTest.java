package com.xm.gamehub.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Test
    public void testConcurrentPurchase() throws InterruptedException {
        int threadCount = 10; // 并发线程数
        Long gameId = 39L;    // 测试的游戏ID
        Long userId = 1L;     // 测试的用户ID
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // 启动多个线程同时购买
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    boolean result = gameService.purchaseGame(userId + index, gameId);
                    if (result) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.out.println("Purchase failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        latch.await();
        executorService.shutdown();
        
        System.out.println("Total success purchases: " + successCount.get());
    }
} 