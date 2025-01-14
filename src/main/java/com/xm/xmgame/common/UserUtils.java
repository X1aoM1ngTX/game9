package com.xm.xmgame.common;

import com.xm.xmgame.model.domain.User;

/**
 * 用户工具类
 *
 * @author XMTX8yyds
 */
public class UserUtils {

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户
     * @return 脱敏后的用户
     */
    public static User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setUserId(originUser.getUserId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserEmail(originUser.getUserEmail());
        safetyUser.setUserPhone(originUser.getUserPhone());
        safetyUser.setUserCreatedTime(originUser.getUserCreatedTime());
        safetyUser.setUserIsAdmin(originUser.getUserIsAdmin());
        return safetyUser;
    }
}
