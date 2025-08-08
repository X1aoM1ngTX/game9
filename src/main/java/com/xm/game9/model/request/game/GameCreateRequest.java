package com.xm.game9.model.request.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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

    @Serial
    private static final long serialVersionUID = -7982351924864910L;
    @Schema(description = "游戏名称")
    private String gameName;
    @Schema(description = "游戏描述")
    private String gameDescription;
    @Schema(description = "游戏价格")
    private BigDecimal gamePrice;
    @Schema(description = "游戏库存")
    private Integer gameStock;
    @Schema(description = "Steam应用ID")
    private String gameAppId;
}
