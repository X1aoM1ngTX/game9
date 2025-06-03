package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.FriendRelationshipMapper;
import com.xm.game9.model.domain.FriendRelationship;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.service.FriendService;
import com.xm.game9.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

    @Resource
    private UserService userService;

    @Resource
    private FriendRelationshipMapper friendRelationshipMapper;

    /** 最大好友数量 */
    private static final int MAX_FRIEND_COUNT = 100;

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

    @Override
    public List<FriendVO> getFriendList(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectFriendsByUserId(userId);
    }

    @Override
    public List<FriendVO> getPendingFriendRequests(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectPendingRequests(userId);
    }

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
}