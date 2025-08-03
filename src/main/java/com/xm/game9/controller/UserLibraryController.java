package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.userLibrary.UserGameRequest;
import com.xm.game9.service.UserLibraryService;
import com.xm.game9.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户游戏库控制器
 * 
 * @author X1aoM1ngTX
 */
@Tag(name = "用户游戏库接口", description = "用户游戏库相关的所有接口")
@RestController
@RequestMapping("/userLibrary")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@Slf4j
public class UserLibraryController {

    @Autowired
    private UserLibraryService userLibraryService;

    @Autowired
    private UserService userService;

    /**
     * 添加游戏到用户游戏库
     *
     * @param addGameRequest 游戏请求体
     * @param request        HTTP请求
     * @return 是否添加成功
     */
    @Operation(summary = "添加游戏到用户游戏库", description = "添加游戏到用户游戏库")
    @PostMapping("/addGameToLibrary")
    public BaseResponse<Boolean> addUserGame(@RequestBody UserGameRequest addGameRequest, HttpServletRequest request) {
        if (addGameRequest == null || addGameRequest.getGameId() == null || addGameRequest.getGameId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userLibraryService.addUserGame(loginUser.getUserId(), addGameRequest.getGameId());
        return ResultUtils.success(result);
    }

    /**
     * 从用户游戏库移除游戏
     *
     * @param removeGameRequest 游戏请求体
     * @param request           HTTP请求
     * @return 是否移除成功
     */
    @Operation(summary = "从用户游戏库移除游戏", description = "从用户游戏库移除游戏")
    @PostMapping("/removeGameFromLibrary")
    public BaseResponse<Boolean> removeUserGame(@RequestBody UserGameRequest removeGameRequest, HttpServletRequest request) {
        if (removeGameRequest == null || removeGameRequest.getGameId() == null || removeGameRequest.getGameId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userLibraryService.removeUserGame(loginUser.getUserId(), removeGameRequest.getGameId());
        return ResultUtils.success(result);
    }

    /**
     * 获取登录用户的游戏库列表
     *
     * @param request HTTP请求
     * @return 游戏列表
     */
    @Operation(summary = "获取登录用户的游戏库列表", description = "获取登录用户的游戏库列表")
    @GetMapping("/listSelfGames")
    public BaseResponse<List<Game>> listSelfGames(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Game> games = userLibraryService.getUserGames(loginUser.getUserId());
        return ResultUtils.success(games);
    }

    /**
     * 获取用户的游戏库列表
     *
     * @param request HTTP请求
     * @return 游戏列表
     */
    @Operation(summary = "获取用户的游戏库列表", description = "获取用户的游戏库列表")
    @GetMapping("/listUserGames/{userId}")
    public BaseResponse<List<Game>> listUserGamesByUserId(@PathVariable Long userId) {
        List<Game> games = userLibraryService.getUserGames(userId);
        return ResultUtils.success(games);
    }
}
