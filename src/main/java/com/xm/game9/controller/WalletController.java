package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.constant.UserConstant;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.wallet.WalletRechargeRequest;
import com.xm.game9.model.request.wallet.WalletConsumeRequest;
import com.xm.game9.model.vo.wallet.WalletVO;
import com.xm.game9.model.vo.wallet.WalletTransactionVO;
import com.xm.game9.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包接口
 *
 * @author Game9
 */
@Tag(name = "钱包接口", description = "钱包相关的所有接口")
@RestController
@RequestMapping("/wallet")
@Validated
@Slf4j
public class WalletController {

    @Resource
    private WalletService walletService;

    /**
     * 获取钱包信息
     *
     * @param request HTTP请求
     * @return 钱包信息
     */
    @Operation(summary = "获取钱包信息", description = "获取当前用户的钱包信息")
    @GetMapping("/info")
    public BaseResponse<WalletVO> getWalletInfo(HttpServletRequest request) {
        // 获取当前登录用户
        Long userId = getCurrentUserId(request);
        WalletVO walletVO = walletService.getWalletByUserId(userId);
        return ResultUtils.success(walletVO);
    }

    /**
     * 钱包充值
     *
     * @param rechargeRequest 充值请求
     * @param request        HTTP请求
     * @return 充值后的钱包信息
     */
    @Operation(summary = "钱包充值", description = "钱包充值")
    @PostMapping("/recharge")
    public BaseResponse<WalletVO> recharge(@RequestBody @Valid WalletRechargeRequest rechargeRequest, 
                                          HttpServletRequest request) {
        // 设置当前用户ID
        rechargeRequest.setUserId(getCurrentUserId(request));
        WalletVO walletVO = walletService.recharge(rechargeRequest);
        return ResultUtils.success(walletVO);
    }

    /**
     * 钱包消费
     *
     * @param consumeRequest 消费请求
     * @param request       HTTP请求
     * @return 消费后的钱包信息
     */
    @Operation(summary = "钱包消费", description = "钱包消费")
    @PostMapping("/consume")
    public BaseResponse<WalletVO> consume(@RequestBody @Valid WalletConsumeRequest consumeRequest, 
                                         HttpServletRequest request) {
        // 设置当前用户ID
        consumeRequest.setUserId(getCurrentUserId(request));
        WalletVO walletVO = walletService.consume(consumeRequest);
        return ResultUtils.success(walletVO);
    }

    /**
     * 获取钱包余额
     *
     * @param request HTTP请求
     * @return 钱包余额
     */
    @Operation(summary = "获取钱包余额", description = "获取当前用户的钱包余额")
    @GetMapping("/balance")
    public BaseResponse<BigDecimal> getBalance(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        BigDecimal balance = walletService.getBalance(userId);
        return ResultUtils.success(balance);
    }

    /**
     * 获取交易记录
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param request  HTTP请求
     * @return 交易记录列表
     */
    @Operation(summary = "获取交易记录", description = "获取当前用户的交易记录")
    @GetMapping("/transactions")
    public BaseResponse<List<WalletTransactionVO>> getTransactions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<WalletTransactionVO> transactions = walletService.getTransactionHistoryWithPage(userId, pageNum, pageSize);
        return ResultUtils.success(transactions);
    }

    /**
     * 获取所有交易记录
     *
     * @param request HTTP请求
     * @return 交易记录列表
     */
    @Operation(summary = "获取所有交易记录", description = "获取当前用户的所有交易记录")
    @GetMapping("/transactions/all")
    public BaseResponse<List<WalletTransactionVO>> getAllTransactions(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<WalletTransactionVO> transactions = walletService.getTransactionHistory(userId);
        return ResultUtils.success(transactions);
    }

    /**
     * 转账
     *
     * @param toUserId    转入用户ID
     * @param amount      转账金额
     * @param description 转账描述
     * @param request     HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "转账", description = "用户间转账")
    @PostMapping("/transfer")
    public BaseResponse<Boolean> transfer(
            @RequestParam @NotNull(message = "转入用户ID不能为空") Long toUserId,
            @RequestParam @NotNull(message = "转账金额不能为空") BigDecimal amount,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        Long fromUserId = getCurrentUserId(request);
        if (StringUtils.isBlank(description)) {
            description = "转账";
        }
        boolean result = walletService.transfer(fromUserId, toUserId, amount, description);
        return ResultUtils.success(result);
    }

    /**
     * 冻结钱包
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "冻结钱包", description = "冻结当前用户的钱包")
    @PostMapping("/freeze")
    public BaseResponse<Boolean> freezeWallet(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean result = walletService.freezeWallet(userId);
        return ResultUtils.success(result);
    }

    /**
     * 解冻钱包
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "解冻钱包", description = "解冻当前用户的钱包")
    @PostMapping("/unfreeze")
    public BaseResponse<Boolean> unfreezeWallet(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean result = walletService.unfreezeWallet(userId);
        return ResultUtils.success(result);
    }

    /**
     * 检查钱包状态
     *
     * @param request HTTP请求
     * @return 钱包状态是否正常
     */
    @Operation(summary = "检查钱包状态", description = "检查当前用户的钱包状态是否正常")
    @GetMapping("/status")
    public BaseResponse<Boolean> checkWalletStatus(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean result = walletService.checkWalletStatus(userId);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户ID
     *
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        User user = (User) userObj;
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        return user.getUserId();
    }
}