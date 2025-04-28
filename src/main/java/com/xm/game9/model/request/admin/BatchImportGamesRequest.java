package com.xm.gamehub.model.request.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BatchImportGamesRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 515684984113288L;
    private List<GameImportInfo> games;

    @Data
    public static class GameImportInfo {
        private String gameName;
        private String gameDescription;
        private BigDecimal gamePrice;
        private Integer gameStock;
    }
} 