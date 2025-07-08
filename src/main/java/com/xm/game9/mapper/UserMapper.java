package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【user(用户表)】的数据库操作Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询未删除的用户
     *
     * @param userName 用户名
     * @return 用户
     */
    @Select("SELECT * FROM user WHERE userIsDelete = 0 AND userName = #{userName}")
    User selectByUserName(@Param("userName") String userName);

    /**
     * 根据邮箱查询未删除的用户
     *
     * @param userEmail 邮箱
     * @return 用户
     */
    @Select("SELECT * FROM user WHERE userIsDelete = 0 AND userEmail = #{userEmail}")
    User selectByEmail(@Param("userEmail") String userEmail);

}




