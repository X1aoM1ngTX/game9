package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户游戏库
 *
 * @表名 userLibrary
 */
@TableName(value = "userLibrary")
@Data
public class UserLibrary implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 410294789136513L;
    /**
     * 用户游戏库ID
     */
    @TableId(type = IdType.AUTO)
    private Long userLibraryId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 游戏ID
     */
    private Long gameId;
}