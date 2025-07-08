package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.friend.FriendAddRequest;
import com.xm.game9.model.vo.FriendGroupVO;
import com.xm.game9.model.vo.FriendOnlineStatusVO;
import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.service.FriendService;
import com.xm.game9.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 好友关系接口
 *
 * @author X1aoM1ngTX
 */
@RestController
@RequestMapping("/friend")
@CrossOrigin(origins = { "http://localhost:3000" }, allowCredentials = "true")
@Tag(name = "好友系统接口", description = "好友系统相关的所有接口")
@Slf4j
public class FriendController {

    @Resource
    private FriendService friendService;

    @Resource
    private UserService userService;

    /**
     * 发送好友请求
     *
     * @param addRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @Operation(summary = "发送好友请求")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> addFriend(@RequestBody FriendAddRequest addRequest, HttpServletRequest request) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (addRequest.getFriendId() == null || addRequest.getFriendId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友ID不能为空");
        }
        if (addRequest.getRemark() != null && addRequest.getRemark().length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "备注长度不能超过50个字符");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.sendFriendRequest(loginUser.getUserId(), addRequest.getFriendId(),
                addRequest.getRemark());
        return ResultUtils.success(result);
    }

    /**
     * 处理好友请求
     *
     * @param friendId
     * @param accept
     * @param request
     * @return
     */
    @PostMapping("/handle")
    @Operation(summary = "处理好友请求")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> handleFriendRequest(@RequestParam Long friendId,
            @RequestParam Boolean accept,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.handleFriendRequest(loginUser.getUserId(), friendId, accept);
        return ResultUtils.success(result);
    }

    /**
     * 获取好友列表
     *
     * @param request
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    @Cacheable(value = "friendList", key = "#request.getSession().getId()", unless = "#result == null")
    public BaseResponse<List<FriendVO>> getFriendList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendVO> friendList = friendService.getFriendList(loginUser.getUserId());
        return ResultUtils.success(friendList);
    }

    /**
     * 获取待处理的好友请求
     *
     * @param request
     * @return
     */
    @GetMapping("/request/list")
    @Operation(summary = "获取待处理的好友请求")
    @Cacheable(value = "pendingRequests", key = "#request.getSession().getId()", unless = "#result == null")
    public BaseResponse<List<FriendVO>> getPendingRequests(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendVO> requests = friendService.getPendingFriendRequests(loginUser.getUserId());
        return ResultUtils.success(requests);
    }

    /**
     * 删除好友
     *
     * @param friendId
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @Operation(summary = "删除好友")
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"friendList", "pendingRequests"}, key = "#request.getSession().getId()")
    public BaseResponse<Boolean> deleteFriend(@RequestParam Long friendId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.deleteFriend(loginUser.getUserId(), friendId);
        return ResultUtils.success(result);
    }

    /** 
     * 修改好友备注
     * 
     * @param friendId
     * @param remark
     * @param request
     * @return
     */
    @PostMapping("/remark")
    @Operation(summary = "修改好友备注")
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "friendList", key = "#request.getSession().getId()")
    public BaseResponse<Boolean> updateRemark(@RequestParam Long friendId,
            @RequestParam String remark,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.updateFriendRemark(loginUser.getUserId(), friendId, remark);
        return ResultUtils.success(result);
    }
}