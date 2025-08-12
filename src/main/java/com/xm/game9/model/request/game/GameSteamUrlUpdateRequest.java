package com.xm.game9.model.request.game;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * Steam URL更新请求
 * 
 * @author X1aoM1ngTX
 */
@Data
@Schema(description = "Steam URL更新请求")
public class GameSteamUrlUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    @NotNull(message = "游戏ID不能为空")
    @Schema(description = "游戏ID", example = "1")
    private Long gameId;

    /**
     * Steam游戏URL
     */
    @NotBlank(message = "Steam URL不能为空")
    @Schema(description = "Steam游戏URL", example = "https://store.steampowered.com/app/1172470/Apex_Legends/")
    private String steamUrl;
}