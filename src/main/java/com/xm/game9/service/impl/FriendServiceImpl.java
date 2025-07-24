package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.FriendRelationshipMapper;
import com.xm.game9.model.domain.FriendRelationship;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.model.vo.FriendRequestVO;
import com.xm.game9.service.FriendService;
import com.xm.game9.service.UserService;
import com.xm.game9.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 好友关系服务实现类
 * @创建时间 2025-05-28
 */
@Service
@Slf4j
public class FriendServiceImpl extends ServiceImpl<FriendRelationshipMapper, FriendRelationship>
        implements FriendService {

    /**
     * 最大好友数量
     */
    private static final int MAX_FRIEND_COUNT = 100;
    @Resource
    private UserService userService;
    @Resource
    private FriendRelationshipMapper friendRelationshipMapper;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 发送好友请求
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @param remark   备注
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendFriendRequest(Long userId, Long friendId, String remark) {
        // 参数校验
        if (userId == null || friendId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 不能添加自己为好友
        if (userId.equals(friendId)) {
            throw new BusinessException(ErrorCode.FRIEND_CANNOT_ADD_SELF, "不能添加自己为好友");
        }

        // 检查目标用户是否存在
        User friend = userService.getById(friendId);
        if (friend == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 检查是否已经是好友
        Long count = lambdaQuery()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendId, friendId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.FRIEND_ALREADY_EXIST, "已经是好友关系");
        }

        // 检查是否已经发送过请求
        count = lambdaQuery()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendId, friendId)
                .eq(FriendRelationship::getFriendStatus, 0)
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_EXIST, "已经发送过好友请求");
        }

        // 检查好友数量是否超限
        count = lambdaQuery()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .count();
        if (count >= MAX_FRIEND_COUNT) {
            throw new BusinessException(ErrorCode.FRIEND_LIMIT_EXCEEDED);
        }

        // 创建好友请求
        FriendRelationship relationship = new FriendRelationship();
        relationship.setUserId(userId);
        relationship.setFriendId(friendId);
        relationship.setFriendStatus(0);
        relationship.setFriendRemark(remark);
        relationship.setFriendCreateTime(new Date());
        relationship.setFriendUpdateTime(new Date());
        relationship.setFriendIsDeleted(0);

        return save(relationship);
    }

    /**
     * 处理好友请求
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @param accept   是否接受请求
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleFriendRequest(Long userId, Long friendId, boolean accept) {
        // 参数校验
        if (userId == null || friendId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 查找好友请求
        FriendRelationship request = lambdaQuery()
                .eq(FriendRelationship::getUserId, friendId)
                .eq(FriendRelationship::getFriendId, userId)
                .eq(FriendRelationship::getFriendStatus, 0)
                .one();

        if (request == null) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND, "找不到相关的好友请求");
        }

        // 检查请求是否已经被处理
        if (request.getFriendStatus() != 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_HANDLED, "好友请求已经被处理");
        }

        // 更新请求状态
        request.setFriendStatus(accept ? 1 : 2);
        request.setFriendUpdateTime(new Date());
        boolean success = updateById(request);

        if (!accept) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_REJECTED, "好友请求被拒绝");
        }

        // 如果接受请求，检查好友数量是否超限
        Long count = lambdaQuery()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .count();
        if (count >= MAX_FRIEND_COUNT) {
            throw new BusinessException(ErrorCode.FRIEND_LIMIT_EXCEEDED, "好友数量超限");
        }

        // 如果接受请求，创建双向好友关系
        if (accept && success) {
            FriendRelationship reverse = new FriendRelationship();
            reverse.setUserId(userId);
            reverse.setFriendId(friendId);
            reverse.setFriendStatus(1);
            reverse.setFriendCreateTime(new Date());
            reverse.setFriendUpdateTime(new Date());
            reverse.setFriendIsDeleted(0);
            success = save(reverse);
        }

        return success;
    }

    /**
     * 获取好友分组列表
     *
     * @param userId 用户ID
     * @return 好友分组列表
     */
    @Override
    @Cacheable(value = "friendList", key = "#userId", unless = "#result == null")
    public List<FriendVO> getFriendList(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        List<FriendVO> friendList = friendRelationshipMapper.selectFriendsByUserId(userId);
        // 查 Redis 并设置 isOnline
        for (FriendVO friend : friendList) {
            boolean isOnline = redisUtil.hasKey("user:online:" + friend.getFriendId());
            friend.setIsOnline(isOnline);
        }
        return friendList;
    }

    /**
     * 获取待处理的好友请求
     *
     * @param userId 用户ID
     * @return 待处理的好友请求列表
     */
    @Override
    public List<FriendVO> getPendingFriendRequests(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectPendingRequests(userId);
    }

    /**
     * 获取好友分组列表
     *
     * @param userId 用户ID
     * @return 好友分组列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查好友关系是否存在
        boolean exists = lambdaQuery()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendId, friendId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .exists();
        if (!exists) {
            throw new BusinessException(ErrorCode.FRIEND_NOT_FOUND, "找不到相关的好友关系");
        }

        // 删除双向好友关系
        boolean success = lambdaUpdate()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendId, friendId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .set(FriendRelationship::getFriendStatus, 3)
                .set(FriendRelationship::getFriendUpdateTime, new Date())
                .update();

        if (success) {
            success = lambdaUpdate()
                    .eq(FriendRelationship::getUserId, friendId)
                    .eq(FriendRelationship::getFriendId, userId)
                    .eq(FriendRelationship::getFriendStatus, 1)
                    .set(FriendRelationship::getFriendStatus, 3)
                    .set(FriendRelationship::getFriendUpdateTime, new Date())
                    .update();
        }

        return success;
    }

    /**
     * 创建好友分组
     *
     * @param userId    用户ID
     * @param groupName 分组名称
     */
    @Override
    public boolean updateFriendRemark(Long userId, Long friendId, String remark) {
        if (userId == null || friendId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查好友关系是否存在
        boolean exists = lambdaQuery()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendId, friendId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .exists();
        if (!exists) {
            throw new BusinessException(ErrorCode.FRIEND_NOT_FOUND, "找不到相关的好友关系");
        }

        return lambdaUpdate()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getFriendId, friendId)
                .eq(FriendRelationship::getFriendStatus, 1)
                .set(FriendRelationship::getFriendRemark, remark)
                .set(FriendRelationship::getFriendUpdateTime, new Date())
                .update();
    }

    /**
     * 获取收到的好友申请
     * 
     * @param userId 用户ID
     * @return 申请列表
     */
    @Override
    public List<FriendRequestVO> getReceivedRequests(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectReceivedRequests(userId);
    }

    @Override
    public List<FriendRequestVO> getSentRequests(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectSentRequests(userId);
    }

    /**
     * 通过申请记录ID处理好友请求
     * 
     * @param userId 当前用户ID
     * @param id 申请记录ID
     * @param accept 是否接受
     * @return 是否成功
     */
    @Override
    public boolean handleFriendRequestById(Long userId, Long id, boolean accept) {
        if (userId == null || id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 查找好友请求
        FriendRelationship request = this.getById(id);
        if (request == null || !request.getFriendId().equals(userId) || request.getFriendStatus() != 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND, "找不到相关的好友请求");
        }
        // 更新请求状态
        request.setFriendStatus(accept ? 1 : 2);
        request.setFriendUpdateTime(new java.util.Date());
        boolean success = this.updateById(request);
        if (!accept) {
            return success;
        }
        // 如果接受请求，创建双向好友关系
        FriendRelationship reverse = new FriendRelationship();
        reverse.setUserId(userId);
        reverse.setFriendId(request.getUserId());
        reverse.setFriendStatus(1);
        reverse.setFriendCreateTime(new java.util.Date());
        reverse.setFriendUpdateTime(new java.util.Date());
        reverse.setFriendIsDeleted(0);
        success = this.save(reverse);
        return success;
    }
}