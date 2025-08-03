package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.WishlistMapper;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.domain.Wishlist;
import com.xm.game9.model.request.wishlist.WishlistAddRequest;
import com.xm.game9.model.request.wishlist.WishlistRemoveRequest;
import com.xm.game9.model.vo.WishlistVO;
import com.xm.game9.service.GameService;
import com.xm.game9.service.UserService;
import com.xm.game9.service.WishlistService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl extends ServiceImpl<WishlistMapper, Wishlist> implements WishlistService {

    @Resource
    private UserService userService;

    @Resource
    private GameService gameService;

    /**
     * 添加游戏到愿望单
     *
     * @param wishlistAddRequest
     * @param request
     * @return
     */
    @Override
    public boolean addGameToWishlist(WishlistAddRequest wishlistAddRequest, HttpServletRequest request) {
        if (wishlistAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        
        // 检查是否已存在相同的记录
        QueryWrapper<Wishlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getUserId());
        queryWrapper.eq("gameId", wishlistAddRequest.getGameId());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该游戏已在愿望单中");
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(loginUser.getUserId());
        wishlist.setGameId(wishlistAddRequest.getGameId());
        boolean save = this.save(wishlist);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败");
        }
        return true;
    }

    /**
     * 从愿望单移除游戏
     *
     * @param wishlistRemoveRequest
     * @param request
     * @return
     */
    @Override
    public boolean removeGameFromWishlist(WishlistRemoveRequest wishlistRemoveRequest, HttpServletRequest request) {
        if (wishlistRemoveRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Wishlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getUserId());
        queryWrapper.eq("gameId", wishlistRemoveRequest.getGameId());
        boolean remove = this.remove(queryWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "移除失败");
        }
        return true;
    }

    /**
     * 获取当前用户的愿望单
     *
     * @param request
     * @return
     */
    @Override
    public List<WishlistVO> getMyWishlist(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Wishlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getUserId());
        queryWrapper.eq("isDeleted", 0); // 只查询未删除的记录
        List<Wishlist> wishlistItems = this.list(queryWrapper);
        if (wishlistItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> gameIds = wishlistItems.stream().map(Wishlist::getGameId).collect(Collectors.toList());
        List<Game> games = gameService.listByIds(gameIds);
        
        // 创建一个Map来快速查找游戏信息
        Map<Long, Game> gameMap = games.stream().collect(Collectors.toMap(Game::getGameId, game -> game));
        
        return wishlistItems.stream().map(wishlist -> {
            WishlistVO wishlistVO = new WishlistVO();
            wishlistVO.setWishlistId(wishlist.getWishlistId());
            wishlistVO.setCreateTime(wishlist.getCreateTime());
            wishlistVO.setGame(gameMap.get(wishlist.getGameId()));
            return wishlistVO;
        }).collect(Collectors.toList());
    }
}
