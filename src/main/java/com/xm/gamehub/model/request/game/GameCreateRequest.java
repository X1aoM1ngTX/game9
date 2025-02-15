package com.xm.gamehub.model.request.game;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 游戏创建请求体
 *
 * @author X1aoM1ngTX
 */
@Schema(description = "游戏创建请求")
@Data
public class GameCreateRequest implements Serializable {

    @Schema(description = "游戏名称")
    private String gameName;

    @Schema(description = "游戏描述")
    private String gameDescription;

    @Schema(description = "游戏价格")
    private BigDecimal gamePrice;

    @Schema(description = "游戏库存")
    private Integer gameStock;

    @Serial
    private static final long serialVersionUID = -7982351924864910L;
}
