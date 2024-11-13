package com.xm.xmgame.mapper;

import com.xm.xmgame.model.domain.Game;
import com.xm.xmgame.model.domain.UserLibrary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author XMTX8yyds
* @description 针对表【userLibrary(用户游戏库)】的数据库操作Mapper
* @createDate 2024-11-13 15:11:22
* @Entity generator.domain.UserLibrary
*/
public interface UserLibraryMapper extends BaseMapper<UserLibrary> {

    @Delete("DELETE FROM UserLibrary WHERE userId = #{userId} AND gameId = #{gameId}")
    int deleteByUserIdAndGameId(Long userId, Long gameId);

    @Select("SELECT g.* FROM Game g JOIN UserLibrary ul ON g.gameId = ul.gameId WHERE ul.userId = #{userId}")
    List<Game> selectGamesByUserId(Long userId);
}




