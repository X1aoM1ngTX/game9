package com.xm.xmgame.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.model.domain.UserLibrary;
import com.xm.xmgame.service.UserLibraryService;
import com.xm.xmgame.mapper.UserLibraryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author XMTX8yyds
 * @描述 针对表【userLibrary(用户游戏库)】的数据库操作Service实现
 * @创建时间 2024-11-13 15:11:22
 */
@Service
public class UserLibraryServiceImpl extends ServiceImpl<UserLibraryMapper, UserLibrary> implements UserLibraryService {

    @Override
    public boolean addUserGame(Long userId, Long gameId) {
        return false;
    }

    @Override
    public boolean removeUserGame(Long userId, Long gameId) {
        return false;
    }

    @Override
    public List<Game> getUserGames(Long userId) {
        return List.of();
    }
}




