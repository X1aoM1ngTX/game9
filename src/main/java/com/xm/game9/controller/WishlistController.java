package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.model.request.wishlist.WishlistAddRequest;
import com.xm.game9.model.request.wishlist.WishlistRemoveRequest;
import com.xm.game9.model.vo.WishlistVO;
import com.xm.game9.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 愿望单控制器
 * 
 * @author X1aoM1ngTX
 */
@Tag(name = "愿望单接口", description = "愿望单相关的所有接口")
@RestController
@RequestMapping("/wishlist")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@Slf4j
public class WishlistController {

    @Resource
    private WishlistService wishlistService;

    @Operation(summary = "添加游戏到愿望单", description = "将指定游戏添加到当前用户的愿望单")
    @PostMapping("/add")
    public BaseResponse<Boolean> addGameToWishlist(@RequestBody WishlistAddRequest wishlistAddRequest, HttpServletRequest request) {
        boolean result = wishlistService.addGameToWishlist(wishlistAddRequest, request);
        return ResultUtils.success(result);
    }

    @Operation(summary = "从愿望单移除游戏", description = "从当前用户的愿望单中移除指定游戏")
    @PostMapping("/remove")
    public BaseResponse<Boolean> removeGameFromWishlist(@RequestBody WishlistRemoveRequest wishlistRemoveRequest, HttpServletRequest request) {
        boolean result = wishlistService.removeGameFromWishlist(wishlistRemoveRequest, request);
        return ResultUtils.success(result);
    }

    @Operation(summary = "获取当前用户的愿望单", description = "获取当前登录用户的所有愿望单项目")
    @GetMapping("/my")
    public BaseResponse<List<WishlistVO>> getMyWishlist(HttpServletRequest request) {
        List<WishlistVO> wishlist = wishlistService.getMyWishlist(request);
        return ResultUtils.success(wishlist);
    }
}
