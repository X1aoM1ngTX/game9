package com.xm.gamehub.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 游戏查询请求
 *
 * @author X1aoM1ngTX
 */
@Data
public class GameQueryRequest implements Serializable {
    /**
     * 游戏名称（支持模糊查询）
     */
    private String gameName;

    /**
     * 当前页号
     */
    private Integer current = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

    /**
     * 是否只显示未下架
     */
    private Boolean showAvailableOnly;

    @Serial
    private static final long serialVersionUID = 9874155688913L;
} 