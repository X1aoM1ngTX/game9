package com.xm.game9.model.vo;

import com.xm.game9.model.domain.Game;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class WishlistVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long wishlistId;
    
    private Game game;
    
    private Date createTime;
}
