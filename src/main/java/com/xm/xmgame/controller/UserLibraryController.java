package com.xm.xmgame.controller;

import com.xm.xmgame.common.BaseResponse;
import com.xm.xmgame.common.ResultUtils;
import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.service.UserLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userLibrary")
public class UserLibraryController {

    @Autowired
    private UserLibraryService userLibraryService;

    @PostMapping("/addUserGame")
    public BaseResponse<Boolean> addUserGame(@RequestParam Long userId, @RequestParam Long gameId) {
        boolean result = userLibraryService.addUserGame(userId, gameId);
        return ResultUtils.success(result);
    }

    @DeleteMapping("/removeUserGame")
    public BaseResponse<Boolean> removeUserGame(@RequestParam Long userId, @RequestParam Long gameId) {
        boolean result = userLibraryService.removeUserGame(userId, gameId);
        return ResultUtils.success(result);
    }

    @GetMapping("/listUserGame")
    public BaseResponse<List<Game>> getUserGames(@RequestParam Long userId) {
        List<Game> games = userLibraryService.getUserGames(userId);
        return ResultUtils.success(games);
    }
}
