package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.Wishlist;
import com.xm.game9.model.request.wishlist.WishlistAddRequest;
import com.xm.game9.model.request.wishlist.WishlistRemoveRequest;
import com.xm.game9.model.vo.WishlistVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author xm
 * @description 针对表【wishlist】的数据库操作Service
 * @createDate 2023-12-07 15:23:23
 */
public interface WishlistService extends IService<Wishlist> {

    /**
     * 添加游戏到愿望单
     *
     * @param wishlistAddRequest
     * @param request
     * @return
     */
    boolean addGameToWishlist(WishlistAddRequest wishlistAddRequest, HttpServletRequest request);

    /**
     * 从愿望单移除游戏
     *
     * @param wishlistRemoveRequest
     * @param request
     * @return
     */
    boolean removeGameFromWishlist(WishlistRemoveRequest wishlistRemoveRequest, HttpServletRequest request);

    /**
     * 获取当前用户的愿望单
     *
     * @param request
     * @return
     */
    List<WishlistVO> getMyWishlist(HttpServletRequest request);
}
