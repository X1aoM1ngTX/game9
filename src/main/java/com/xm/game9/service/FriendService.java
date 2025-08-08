package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.FriendRelationship;
import com.xm.game9.model.vo.FriendRequestVO;
import com.xm.game9.model.vo.FriendVO;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 好友关系服务接口
 * @创建时间 2025-05-28
 */
public interface FriendService extends IService<FriendRelationship> {

    /**
     * 发送好友请求
     *
     * @param userId   发送请求的用户ID
     * @param friendId 接收请求的用户ID
     * @param remark   好友备注
     * @return 是否发送成功
     */
    boolean sendFriendRequest(Long userId, Long friendId, String remark);

    /**
     * 处理好友请求
     *
     * @param userId   处理请求的用户ID
     * @param friendId 发送请求的用户ID
     * @param accept   是否接受请求
     * @return 是否处理成功
     */
    boolean handleFriendRequest(Long userId, Long friendId, boolean accept);

    /**
     * 获取好友列表
     *
     * @param userId 用户ID
     * @return 好友列表
     */
    List<FriendVO> getFriendList(Long userId);

    /**
     * 获取待处理的好友请求
     *
     * @param userId 用户ID
     * @return 待处理的好友请求列表
     */
    List<FriendVO> getPendingFriendRequests(Long userId);

    /**
     * 删除好友
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return 是否删除成功
     */
    boolean deleteFriend(Long userId, Long friendId);

    /**
     * 修改好友备注
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @param remark   新的备注
     * @return 是否修改成功
     */
    boolean updateFriendRemark(Long userId, Long friendId, String remark);

    /**
     * 获取收到的好友申请
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    List<FriendRequestVO> getReceivedRequests(Long userId);

    /**
     * 获取我发出的好友申请
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    List<FriendRequestVO> getSentRequests(Long userId);

    /**
     * 通过申请记录ID处理好友请求
     *
     * @param userId 当前用户ID
     * @param id     申请记录ID
     * @param accept 是否接受
     * @return 是否成功
     */
    boolean handleFriendRequestById(Long userId, Long id, boolean accept);
} 