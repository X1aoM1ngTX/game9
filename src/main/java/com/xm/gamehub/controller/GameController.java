package com.xm.gamehub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.gamehub.common.BaseResponse;
import com.xm.gamehub.common.ErrorCode;
import com.xm.gamehub.common.ResultUtils;
import com.xm.gamehub.exception.BusinessException;
import com.xm.gamehub.model.domain.Game;
import com.xm.gamehub.model.domain.User;
import com.xm.gamehub.model.request.game.GameCreateRequest;
import com.xm.gamehub.model.request.game.GameQueryRequest;
import com.xm.gamehub.model.request.game.GameStatusRequest;
import com.xm.gamehub.model.request.game.GameUpdateRequest;
import com.xm.gamehub.model.vo.GameDetailVO;
import com.xm.gamehub.service.GameService;
import com.xm.gamehub.utils.UploadUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;

import static com.xm.gamehub.constant.UserConstant.ADMIN_ROLE;
import static com.xm.gamehub.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 游戏接口
 */
@Tag(name = "游戏接口", description = "游戏相关的所有接口")
@RestController
@RequestMapping("/game")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class GameController {

    @Resource
    private GameService gameService;

    /**
     * 创建游戏
     *
     * @param gameCreateRequest 游戏创建请求
     * @return 游戏ID
     */
    @Operation(summary = "创建游戏", description = "创建新的游戏")
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
    @Operation(summary = "查询所有游戏", description = "查询所有游戏")
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
    @Operation(summary = "查询游戏（分页）", description = "查询游戏")
    @PostMapping("/list/page")
    public BaseResponse<Page<Game>> listGames(@RequestBody GameQueryRequest gameQueryRequest) {
        if (gameQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Game> gamePage = gameService.pageGames(gameQueryRequest);
        return ResultUtils.success(gamePage);
    }

    /**
     * 获取游戏详情
     *
     * @param gameId 游戏id
     * @return 游戏详情
     */
    @Operation(summary = "获取游戏详情", description = "根据游戏ID获取游戏的详细信息")
    @GetMapping("/{gameId}")
    public BaseResponse<GameDetailVO> getGameDetail(@Parameter(description = "游戏ID") @PathVariable Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误");
        }
        GameDetailVO gameDetail = gameService.getGameDetail(gameId);
        return ResultUtils.success(gameDetail);
    }

    /**
     * 更新游戏
     *
     * @param gameUpdateRequest 游戏更新请求
     * @return boolean (是否更新成功)
     */
    @Operation(summary = "更新游戏", description = "更新游戏")
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
    @Operation(summary = "设置游戏状态", description = "设置游戏状态")
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
     * 上传游戏封面
     *
     * @param file    游戏封面文件
     * @param request HttpServletRequest
     * @return 游戏封面访问路径
     */
    @Operation(summary = "上传游戏封面", description = "上传游戏封面")
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 检查管理员权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }
        try {
            String url = new UploadUtil().uploadAliyunOss(file);
            return ResultUtils.success(url);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    /**
     * 删除游戏(物理删除)
     *
     * @param gameId  游戏id
     * @param request HttpServlet请求
     * @return 是否删除成功
     */
    @Operation(summary = "删除游戏（物理删除）", description = "删除游戏")
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
        if (userObj == null) {
            return false;
        }
        User user = (User) userObj;
        return user.getUserIsAdmin() == ADMIN_ROLE;
    }
}