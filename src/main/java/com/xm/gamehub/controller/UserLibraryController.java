package com.xm.gamehub.controller;

import com.xm.gamehub.common.BaseResponse;
import com.xm.gamehub.common.ErrorCode;
import com.xm.gamehub.common.ResultUtils;
import com.xm.gamehub.exception.BusinessException;
import com.xm.gamehub.model.domain.Game;
import com.xm.gamehub.model.domain.User;
import com.xm.gamehub.service.UserLibraryService;
import com.xm.gamehub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userLibrary")
@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
@Slf4j
public class UserLibraryController {

    @Autowired
    private UserLibraryService userLibraryService;

    @Autowired
    private UserService userService;

    /**
     * 添加游戏到用户游戏库
     *
     * @param gameId  游戏id
     * @param request HttpServletRequest
     * @return 是否添加成功
     */
    @PostMapping("/addGameToLibrary")
    public BaseResponse<Boolean> addUserGame(@RequestParam Long gameId, HttpServletRequest request) {
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userLibraryService.addUserGame(loginUser.getUserId(), gameId);
        return ResultUtils.success(result);
    }

    /**
     * 从用户游戏库移除游戏
     *
     * @param gameId  游戏id
     * @param request HttpServletRequest
     * @return 是否移除成功
     */
    @PostMapping("/removeGameFromLibrary")
    public BaseResponse<Boolean> removeUserGame(@RequestParam Long gameId, HttpServletRequest request) {
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userLibraryService.removeUserGame(loginUser.getUserId(), gameId);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户的游戏库列表
     *
     * @param request HttpServletRequest
     * @return 游戏列表
     */
    @GetMapping("/listUserGames")
    public BaseResponse<List<Game>> listUserGames(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Game> games = userLibraryService.getUserGames(loginUser.getUserId());
        return ResultUtils.success(games);
    }
}
