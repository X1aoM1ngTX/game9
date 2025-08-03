package com.xm.game9.model.request.wishlist;

import lombok.Data;

import java.io.Serializable;

@Data
public class WishlistRemoveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long gameId;
}
