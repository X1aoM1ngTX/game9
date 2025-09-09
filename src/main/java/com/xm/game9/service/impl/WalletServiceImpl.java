package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.WalletMapper;
import com.xm.game9.mapper.WalletTransactionMapper;
import com.xm.game9.model.domain.Wallet;
import com.xm.game9.model.domain.WalletTransaction;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.wallet.WalletRechargeRequest;
import com.xm.game9.model.request.wallet.WalletConsumeRequest;
import com.xm.game9.model.vo.wallet.WalletVO;
import com.xm.game9.model.vo.wallet.WalletTransactionVO;
import com.xm.game9.service.WalletService;
import com.xm.game9.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 钱包服务实现类
 *
 * @author Game9
 * @描述 钱包服务实现类
 */
@Service
@Slf4j
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements WalletService {

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private UserService userService;

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    @Override
    public WalletVO getWalletByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        Wallet wallet = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        
        if (wallet == null) {
            // 如果用户没有钱包，自动创建一个
            wallet = createWallet(userId);
        }

        return convertToWalletVO(wallet);
    }

    @Override
    public Wallet createWallet(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查是否已有钱包
        Wallet existingWallet = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        if (existingWallet != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已有钱包");
        }

        // 创建新钱包
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setWalletBalance(ZERO);
        wallet.setWalletStatus(1); // 正常状态
        wallet.setCreatedTime(LocalDateTime.now());
        wallet.setUpdateTime(LocalDateTime.now());

        boolean saved = save(wallet);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建钱包失败");
        }

        log.info("创建钱包成功 - userId: {}", userId);
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletVO recharge(WalletRechargeRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值请求不能为空");
        }

        Long userId = request.getUserId();
        BigDecimal amount = request.getAmount();

        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值金额必须大于0");
        }

        // 使用悲观锁锁定钱包记录
        Wallet wallet = walletMapper.selectByUserIdForUpdate(userId);
        if (wallet == null) {
            // 如果用户没有钱包，自动创建一个
            wallet = createWallet(userId);
            wallet = walletMapper.selectByUserIdForUpdate(userId);
        }

        // 检查钱包状态
        if (wallet.getWalletStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "钱包已冻结，无法充值");
        }

        // 更新钱包余额
        BigDecimal newBalance = wallet.getWalletBalance().add(amount);
        wallet.setWalletBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(wallet);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "充值失败");
        }

        // 创建交易记录
        createTransactionRecord(userId, amount, newBalance, 1, 
                "钱包充值 - " + request.getPaymentMethod(), 
                null, request.getThirdPartyTransactionId());

        log.info("钱包充值成功 - userId: {}, amount: {}, newBalance: {}", userId, amount, newBalance);
        return convertToWalletVO(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletVO consume(WalletConsumeRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消费请求不能为空");
        }

        Long userId = request.getUserId();
        BigDecimal amount = request.getAmount();

        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消费金额必须大于0");
        }

        // 使用悲观锁锁定钱包记录
        Wallet wallet = walletMapper.selectByUserIdForUpdate(userId);
        if (wallet == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户钱包不存在");
        }

        // 检查钱包状态
        if (wallet.getWalletStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "钱包已冻结，无法消费");
        }

        // 检查余额是否充足
        if (wallet.getWalletBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足");
        }

        // 更新钱包余额
        BigDecimal newBalance = wallet.getWalletBalance().subtract(amount);
        wallet.setWalletBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(wallet);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消费失败");
        }

        // 创建交易记录
        createTransactionRecord(userId, amount, newBalance, 2, 
                request.getDescription(), request.getOrderId(), null);

        log.info("钱包消费成功 - userId: {}, amount: {}, newBalance: {}", userId, amount, newBalance);
        return convertToWalletVO(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean internalConsume(Long userId, BigDecimal amount, String description, Long orderId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消费金额必须大于0");
        }

        // 使用悲观锁锁定钱包记录
        Wallet wallet = walletMapper.selectByUserIdForUpdate(userId);
        if (wallet == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户钱包不存在");
        }

        // 检查钱包状态
        if (wallet.getWalletStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "钱包已冻结，无法消费");
        }

        // 检查余额是否充足
        if (wallet.getWalletBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足");
        }

        // 更新钱包余额
        BigDecimal newBalance = wallet.getWalletBalance().subtract(amount);
        wallet.setWalletBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(wallet);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消费失败");
        }

        // 创建交易记录
        createTransactionRecord(userId, amount, newBalance, 2, description, orderId, null);

        log.info("内部消费成功 - userId: {}, amount: {}, newBalance: {}", userId, amount, newBalance);
        return true;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        Wallet wallet = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        
        if (wallet == null) {
            return ZERO;
        }

        return wallet.getWalletBalance();
    }

    @Override
    public List<WalletTransactionVO> getTransactionHistory(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getUserId, userId)
               .orderByDesc(WalletTransaction::getCreatedTime);

        List<WalletTransaction> transactions = walletTransactionMapper.selectList(wrapper);
        return transactions.stream()
                .map(this::convertToWalletTransactionVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletTransactionVO> getTransactionHistoryWithPage(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        Page<WalletTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getUserId, userId)
               .orderByDesc(WalletTransaction::getCreatedTime);

        Page<WalletTransaction> result = walletTransactionMapper.selectPage(page, wrapper);
        return result.getRecords().stream()
                .map(this::convertToWalletTransactionVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeWallet(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        Wallet wallet = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        
        if (wallet == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户钱包不存在");
        }

        if (wallet.getWalletStatus() == 0) {
            return true; // 已经是冻结状态
        }

        wallet.setWalletStatus(0);
        wallet.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(wallet);
        if (updated) {
            log.info("钱包冻结成功 - userId: {}", userId);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeWallet(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        Wallet wallet = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        
        if (wallet == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户钱包不存在");
        }

        if (wallet.getWalletStatus() == 1) {
            return true; // 已经是正常状态
        }

        wallet.setWalletStatus(1);
        wallet.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(wallet);
        if (updated) {
            log.info("钱包解冻成功 - userId: {}", userId);
        }
        return updated;
    }

    @Override
    public boolean checkWalletStatus(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }

        Wallet wallet = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        
        if (wallet == null) {
            return false;
        }

        return wallet.getWalletStatus() == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transfer(Long fromUserId, Long toUserId, BigDecimal amount, String description) {
        if (fromUserId == null || fromUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "转出用户ID不合法");
        }
        if (toUserId == null || toUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "转入用户ID不合法");
        }
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "转账金额必须大于0");
        }
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能给自己转账");
        }

        // 检查转入用户是否存在
        User toUser = userService.getById(toUserId);
        if (toUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "转入用户不存在");
        }

        // 使用悲观锁锁定两个钱包记录
        Wallet fromWallet = walletMapper.selectByUserIdForUpdate(fromUserId);
        Wallet toWallet = walletMapper.selectByUserIdForUpdate(toUserId);

        if (fromWallet == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "转出用户钱包不存在");
        }
        if (toWallet == null) {
            // 如果转入用户没有钱包，自动创建一个
            toWallet = createWallet(toUserId);
            toWallet = walletMapper.selectByUserIdForUpdate(toUserId);
        }

        // 检查钱包状态
        if (fromWallet.getWalletStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "转出用户钱包已冻结");
        }
        if (toWallet.getWalletStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "转入用户钱包已冻结");
        }

        // 检查余额是否充足
        if (fromWallet.getWalletBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足");
        }

        // 更新钱包余额
        BigDecimal fromNewBalance = fromWallet.getWalletBalance().subtract(amount);
        BigDecimal toNewBalance = toWallet.getWalletBalance().add(amount);

        fromWallet.setWalletBalance(fromNewBalance);
        fromWallet.setUpdateTime(LocalDateTime.now());

        toWallet.setWalletBalance(toNewBalance);
        toWallet.setUpdateTime(LocalDateTime.now());

        boolean fromUpdated = updateById(fromWallet);
        boolean toUpdated = updateById(toWallet);

        if (!fromUpdated || !toUpdated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "转账失败");
        }

        // 创建交易记录
        String transferDesc = "转账给" + toUser.getUserName() + " - " + description;
        createTransactionRecord(fromUserId, amount, fromNewBalance, 4, transferDesc, null, null);

        String receiveDesc = "收到" + userService.getById(fromUserId).getUserName() + "的转账 - " + description;
        createTransactionRecord(toUserId, amount, toNewBalance, 3, receiveDesc, null, null);

        log.info("转账成功 - fromUserId: {}, toUserId: {}, amount: {}", fromUserId, toUserId, amount);
        return true;
    }

    /**
     * 创建交易记录
     *
     * @param userId 用户ID
     * @param amount 交易金额
     * @param balanceAfter 交易后余额
     * @param transactionType 交易类型
     * @param description 交易描述
     * @param orderId 关联订单ID
     * @param thirdPartyTransactionId 第三方交易号
     */
    private void createTransactionRecord(Long userId, BigDecimal amount, BigDecimal balanceAfter, 
                                        Integer transactionType, String description, Long orderId, 
                                        String thirdPartyTransactionId) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionDescription(description);
        transaction.setOrderId(orderId);
        transaction.setThirdPartyTransactionId(thirdPartyTransactionId);
        transaction.setCreatedTime(LocalDateTime.now());

        boolean saved = walletTransactionMapper.insert(transaction) > 0;
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建交易记录失败");
        }
    }

    /**
     * 转换为钱包视图对象
     *
     * @param wallet 钱包实体
     * @return 钱包视图对象
     */
    private WalletVO convertToWalletVO(Wallet wallet) {
        WalletVO walletVO = new WalletVO();
        BeanUtils.copyProperties(wallet, walletVO);
        
        // 设置用户名
        User user = userService.getById(wallet.getUserId());
        if (user != null) {
            walletVO.setUserName(user.getUserName());
        }
        
        // 设置钱包状态描述
        if (wallet.getWalletStatus() != null) {
            walletVO.setWalletStatusDesc(wallet.getWalletStatus() == 1 ? "正常" : "冻结");
        }
        
        return walletVO;
    }

    /**
     * 转换为钱包交易记录视图对象
     *
     * @param transaction 交易记录实体
     * @return 交易记录视图对象
     */
    private WalletTransactionVO convertToWalletTransactionVO(WalletTransaction transaction) {
        WalletTransactionVO vo = new WalletTransactionVO();
        BeanUtils.copyProperties(transaction, vo);
        
        // 设置用户名
        User user = userService.getById(transaction.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
        }
        
        // 设置交易类型描述
        if (transaction.getTransactionType() != null) {
            String typeDesc = switch (transaction.getTransactionType()) {
                case 1 -> "充值";
                case 2 -> "消费";
                case 3 -> "转账收入";
                case 4 -> "转账支出";
                default -> "未知";
            };
            vo.setTransactionTypeDesc(typeDesc);
        }
        
        return vo;
    }
}