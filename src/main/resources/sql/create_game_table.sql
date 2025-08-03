-- 游戏表
create table game
(
    gameId               bigint auto_increment comment '游戏ID' primary key,
    gameName             varchar(256)                        not null comment '游戏名',
    gameDescription      text null comment '游戏描述',
    gamePrice            decimal(10, 2)                      not null comment '游戏原始价格',
    gameStock            int                                 not null comment '游戏库存',
    gameCreatedTime      timestamp default CURRENT_TIMESTAMP not null comment '游戏创建时间',
    gameReleaseDate      date null comment '游戏发行时间',
    gameDev              varchar(256) null comment '游戏开发商',
    gamePub              varchar(256) null comment '游戏发行商',
    gameIsRemoved        tinyint(1) default 0 not null comment '游戏是否下架',
    gameDiscount         decimal(5, 2) null comment '游戏折扣',
    gameDiscountedPrices decimal(10, 2) null comment '游戏打折价格',
    gameOnSale           tinyint(1) default 0 not null comment '游戏是否正在打折',
    gameSaleStartTime    datetime null comment '游戏折扣开始时间',
    gameSaleEndTime      datetime null comment '游戏折扣结束时间',
    gameCover            varchar(512) null comment '游戏封面',
    gameAppId            varchar(12) null comment '游戏应用ID',
) comment '游戏表';

DELIMITER
//
CREATE TRIGGER game9_set_gameSaleTime_to_null
    BEFORE UPDATE
    ON game9.game
    FOR EACH ROW
BEGIN
    IF NEW.gameOnSale = 0 THEN
        SET NEW.gameSaleStartTime = NULL;
        SET NEW.gameSaleEndTime = NULL;
END IF;
END
//
DELIMITER ;

DELIMITER
//
CREATE TRIGGER game9_calculate_discounted_price
    BEFORE INSERT
    ON game9.game
    FOR EACH ROW
BEGIN
    IF NEW.gameDiscount IS NOT NULL THEN
        SET NEW.gameDiscountedPrices = NEW.gamePrice * (1 - NEW.gameDiscount / 100);
END IF;
END
//
DELIMITER ;