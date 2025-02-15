package com.xm.xmgame.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.xmgame.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【user(用户表)】的数据库操作Mapper
 * @创建时间 2024-11-11 15:15:35
 * @实体 model.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询未删除的用户
     */
    @Select("SELECT * FROM user WHERE userIsDelete = 0 AND userName = #{userName}")
    User selectByUserName(@Param("userName") String userName);

    /**
     * 根据邮箱查询未删除的用户
     */
    @Select("SELECT * FROM user WHERE userIsDelete = 0 AND userEmail = #{userEmail}")
    User selectByEmail(@Param("userEmail") String userEmail);

}




