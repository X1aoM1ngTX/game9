package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.FriendGroupMapper;
import com.xm.game9.mapper.FriendRelationshipMapper;
import com.xm.game9.model.domain.FriendGroup;
import com.xm.game9.model.domain.FriendRelationship;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.vo.FriendGroupVO;
import com.xm.game9.model.vo.FriendOnlineStatusVO;
import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.service.FriendService;
import com.xm.game9.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

    @Resource
    private UserService userService;

    @Resource
    private FriendRelationshipMapper friendRelationshipMapper;
    
    @Resource
    private FriendGroupMapper friendGroupMapper;

    /** 最大好友数量 */
    private static final int MAX_FRIEND_COUNT = 100;

    /**
     * 发送好友请求
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param remark 备注
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
        relationship.setFriendCreatedTime(new Date());
        relationship.setFriendUpdatedTime(new Date());
        relationship.setFriendIsDelete(0);

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
        request.setFriendUpdatedTime(new Date());
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
            reverse.setFriendCreatedTime(new Date());
            reverse.setFriendUpdatedTime(new Date());
            reverse.setFriendIsDelete(0);
            success = save(reverse);
        }

        return success;
    }

    @Override
    @Cacheable(value = "friendList", key = "#userId", unless = "#result == null")
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
                .set(FriendRelationship::getFriendUpdatedTime, new Date())
                .update();

        if (success) {
            success = lambdaUpdate()
                    .eq(FriendRelationship::getUserId, friendId)
                    .eq(FriendRelationship::getFriendId, userId)
                    .eq(FriendRelationship::getFriendStatus, 1)
                    .set(FriendRelationship::getFriendStatus, 3)
                    .set(FriendRelationship::getFriendUpdatedTime, new Date())
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
                .set(FriendRelationship::getFriendUpdatedTime, new Date())
                .update();
    }

    /**
     * 获取好友分组列表
     * 
     * @param userId 用户ID
     * @return 好友分组列表
     */
    @Override
    public List<FriendVO> searchFriends(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.searchFriends(userId, keyword.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawFriendRequest(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查是否有待处理的请求
        int pendingCount = friendRelationshipMapper.checkPendingRequest(userId, friendId);
        if (pendingCount == 0) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND, "没有找到待处理的好友请求");
        }

        return friendRelationshipMapper.withdrawRequest(userId, friendId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFriendGroup(Long userId, String groupName) {
        if (userId == null || groupName == null || groupName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查分组名称是否已存在
        if (friendGroupMapper.existsByUserIdAndGroupName(userId, groupName.trim())) {
            throw new BusinessException(ErrorCode.FRIEND_GROUP_EXISTS, "分组名称已存在");
        }

        // 获取最大排序号
        Integer maxOrder = friendGroupMapper.getMaxGroupOrder(userId);
        int newOrder = (maxOrder == null) ? 1 : maxOrder + 1;

        FriendGroup friendGroup = new FriendGroup();
        friendGroup.setUserId(userId);
        friendGroup.setGroupName(groupName.trim());
        friendGroup.setGroupOrder(newOrder);
        friendGroup.setGroupCreateTime(new Date());
        friendGroup.setGroupUpdateTime(new Date());

        return friendGroupMapper.insert(friendGroup) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"friendList", "friendGroups"}, key = "#userId")
    public boolean deleteFriendGroup(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查分组是否存在且属于当前用户
        FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
        if (friendGroup == null || !friendGroup.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_GROUP_NOT_FOUND, "分组不存在");
        }

        // 将该分组下的好友移动到默认分组（groupId = null）
        lambdaUpdate()
                .eq(FriendRelationship::getUserId, userId)
                .eq(FriendRelationship::getGroupId, groupId)
                .set(FriendRelationship::getGroupId, null)
                .set(FriendRelationship::getFriendUpdatedTime, new Date())
                .update();

        // 删除分组
        return friendGroupMapper.deleteById(groupId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFriendGroup(Long userId, Long groupId, String groupName) {
        if (userId == null || groupId == null || groupName == null || groupName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 检查分组是否存在且属于当前用户
        FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
        if (friendGroup == null || !friendGroup.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FRIEND_GROUP_NOT_FOUND, "分组不存在");
        }

        // 检查新分组名称是否已存在（排除当前分组）
        boolean exists = friendGroupMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FriendGroup>()
                        .eq(FriendGroup::getUserId, userId)
                        .eq(FriendGroup::getGroupName, groupName.trim())
                        .ne(FriendGroup::getGroupId, groupId)
        ).size() > 0;
        
        if (exists) {
            throw new BusinessException(ErrorCode.FRIEND_GROUP_EXISTS, "分组名称已存在");
        }

        friendGroup.setGroupName(groupName.trim());
        friendGroup.setGroupUpdateTime(new Date());
        return friendGroupMapper.updateById(friendGroup) > 0;
    }

    @Override
    @Cacheable(value = "friendGroups", key = "#userId", unless = "#result == null")
    public List<FriendGroupVO> getFriendGroups(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendGroupMapper.getFriendGroupsWithCount(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"friendList", "friendGroups"}, key = "#userId")
    public boolean moveFriendToGroup(Long userId, Long friendId, Long groupId) {
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

        // 如果指定了分组ID，检查分组是否存在且属于当前用户
        if (groupId != null) {
            FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
            if (friendGroup == null || !friendGroup.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FRIEND_GROUP_NOT_FOUND, "分组不存在");
            }
        }

        return friendRelationshipMapper.updateFriendGroup(userId, friendId, groupId) > 0;
    }

    @Override
    public List<FriendVO> getFriendsByGroup(Long userId, Long groupId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectFriendsByGroup(userId, groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserOnlineStatus(Long userId, boolean isOnline) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        user.setUserIsOnline(isOnline);
        user.setUserLastOnlineTime(new Date());
        return userService.updateById(user);
    }

    @Override
    @Cacheable(value = "friendOnlineStatus", key = "#userId", unless = "#result == null")
    public List<FriendOnlineStatusVO> getFriendsOnlineStatus(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return friendRelationshipMapper.selectFriendsOnlineStatus(userId);
    }
}