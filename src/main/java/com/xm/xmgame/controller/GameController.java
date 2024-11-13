package com.xm.xmgame.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.xmgame.common.BaseResponse;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.common.ResultUtils;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.model.domain.User;
import com.xm.xmgame.model.request.game.GameCreateRequest;
import com.xm.xmgame.model.request.game.GameQueryRequest;
import com.xm.xmgame.model.request.game.GameStatusRequest;
import com.xm.xmgame.model.request.game.GameUpdateRequest;
import com.xm.xmgame.service.GameService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.xm.xmgame.constant.UserConstant.ADMIN_ROLE;
import static com.xm.xmgame.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/game")
public class GameController {

    @Resource
    private GameService gameService;

    /**
     * 创建游戏
     *
     * @param gameCreateRequest 游戏创建请求
     * @return 游戏ID
     */
    @PostMapping("/createGame")
    public BaseResponse<Long> createGame(@RequestBody GameCreateRequest gameCreateRequest, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (gameCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = gameService.createGame(gameCreateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 查询所有游戏
     *
     * @return 所有游戏
     */
    @GetMapping("/getAllGames")
    public BaseResponse<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return ResultUtils.success(games);
    }

    /**
     * 查询游戏
     *
     * @param gameQueryRequest 游戏查询请求
     * @return 游戏查询结果
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Game>> listGames(@RequestBody GameQueryRequest gameQueryRequest) {
        if (gameQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Game> gamePage = gameService.pageGames(gameQueryRequest);
        return ResultUtils.success(gamePage);
    }

    /**
     * 更新游戏
     *
     * @param gameUpdateRequest 游戏更新请求
     * @return boolean (是否更新成功)
     */
    @PutMapping("/updateGame")
    public BaseResponse<Boolean> updateGame(@RequestBody GameUpdateRequest gameUpdateRequest, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (gameUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = gameService.updateGame(gameUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 设置游戏状态
     *
     * @param gameStatusRequest 游戏状态请求
     * @param request           HttpServlet请求
     * @return boolean (是否设置成功)
     */
    @PutMapping("/setGameRemovedStatus")
    public BaseResponse<Boolean> setGameStatus(@RequestBody GameStatusRequest gameStatusRequest,
                                               HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = gameService.setGameRemovedStatus(gameStatusRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除游戏(物理删除)
     *
     * @param gameId 游戏id
     * @param request HttpServlet请求
     * @return 是否删除成功
     */
    @PostMapping("/deleteGame")
    public BaseResponse<Boolean> deleteGame(@RequestBody Long gameId, HttpServletRequest request) {
        // 仅管理员可删除
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = gameService.deleteGame(gameId);
        return ResultUtils.success(result);
    }

    /**
     * 判断是否为管理员
     *
     * @param request HttpServlet请求
     * @return boolean (是否为管理员)
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserIsAdmin() == ADMIN_ROLE;
    }
}