package com.xm.xmgame.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GameUpdateRequest implements Serializable {

    private Long gameId;
    private String gameName;
    private String gameDescription;
    private BigDecimal gamePrice;
    private Integer gameStock;
    private String gamePub;
    private LocalDate gameReleaseDate;
    private String gameDev;
    private Boolean gameIsRemoved;

    @Serial
    private static final long serialVersionUID = -6813351925946917L;

    public boolean isGameIsRemoved() {
        return gameIsRemoved;
    }
}
