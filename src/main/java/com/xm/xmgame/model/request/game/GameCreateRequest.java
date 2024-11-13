package com.xm.xmgame.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 游戏创建请求体
 *
 * @author xm
 */
@Data
public class GameCreateRequest implements Serializable {

    private String gameName;
    private BigDecimal gamePrice;
    private Integer gameStock;

    @Serial
    private static final long serialVersionUID = -7982351924864910L;
}
