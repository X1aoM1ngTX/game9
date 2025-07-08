package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.FriendRelationship;
import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.model.vo.FriendOnlineStatusVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【friendRelationship(好友关系表)】的数据库操作Mapper
 * @创建时间 2025-05-28
 */
public interface FriendRelationshipMapper extends BaseMapper<FriendRelationship> {
    
    /**
     * 获取用户的好友列表
     */
    List<FriendVO> selectFriendsByUserId(@Param("userId") Long userId);
    
    /**
     * 获取待处理的好友请求
     */
    List<FriendVO> selectPendingRequests(@Param("userId") Long userId);

    /**
     * 检查好友关系是否存在
     */
    @Select("SELECT COUNT(*) FROM friendRelationship WHERE userId = #{userId} AND friendId = #{friendId} AND friendStatus = 1 AND friendIsDelete = 0")
    int checkFriendExists(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 检查是否有待处理的好友请求
     */
    @Select("SELECT COUNT(*) FROM friendRelationship WHERE userId = #{userId} AND friendId = #{friendId} AND friendStatus = 0 AND friendIsDelete = 0")
    int checkPendingRequest(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 获取用户的好友数量
     */
    @Select("SELECT COUNT(*) FROM friendRelationship WHERE userId = #{userId} AND friendStatus = 1 AND friendIsDelete = 0")
    int getFriendCount(@Param("userId") Long userId);
} 