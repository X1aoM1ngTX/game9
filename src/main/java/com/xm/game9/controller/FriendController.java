package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.friend.FriendAddRequest;
import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.service.FriendService;
import com.xm.game9.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add")
    @Operation(summary = "发送好友请求")
    public BaseResponse<Boolean> addFriend(@RequestBody FriendAddRequest addRequest, HttpServletRequest request) {
        if (addRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.sendFriendRequest(loginUser.getUserId(), addRequest.getFriendId(),
                addRequest.getRemark());
        return ResultUtils.success(result);
    }

    @PostMapping("/handle")
    @Operation(summary = "处理好友请求")
    public BaseResponse<Boolean> handleFriendRequest(@RequestParam Long friendId,
            @RequestParam Boolean accept,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.handleFriendRequest(loginUser.getUserId(), friendId, accept);
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    public BaseResponse<List<FriendVO>> getFriendList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendVO> friendList = friendService.getFriendList(loginUser.getUserId());
        return ResultUtils.success(friendList);
    }

    @GetMapping("/request/list")
    @Operation(summary = "获取待处理的好友请求")
    public BaseResponse<List<FriendVO>> getPendingRequests(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendVO> requests = friendService.getPendingFriendRequests(loginUser.getUserId());
        return ResultUtils.success(requests);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除好友")
    public BaseResponse<Boolean> deleteFriend(@RequestParam Long friendId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.deleteFriend(loginUser.getUserId(), friendId);
        return ResultUtils.success(result);
    }

    @PostMapping("/remark")
    @Operation(summary = "修改好友备注")
    public BaseResponse<Boolean> updateRemark(@RequestParam Long friendId,
            @RequestParam String remark,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = friendService.updateFriendRemark(loginUser.getUserId(), friendId, remark);
        return ResultUtils.success(result);
    }
}