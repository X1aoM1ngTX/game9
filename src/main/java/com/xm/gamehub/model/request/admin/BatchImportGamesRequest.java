package com.xm.gamehub.model.request.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;

@Data
public class BatchImportGamesRequest implements Serializable {
    private List<GameImportInfo> games;
    
    @Data
    public static class GameImportInfo {
        private String gameName;
        private String gameDescription;
        private BigDecimal gamePrice;
        private Integer gameStock;
    }

    @Serial
    private static final long serialVersionUID = 515684984113288L;
} 