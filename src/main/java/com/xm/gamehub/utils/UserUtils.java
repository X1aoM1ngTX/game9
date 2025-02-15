package com.xm.gamehub.utils;

import com.xm.gamehub.model.domain.User;

/**
 * 用户工具类
 *
 * @author X1aoM1ngTX
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
        safetyUser.setUserNickname(originUser.getUserNickname());
        safetyUser.setUserAvatar(originUser.getUserAvatar());
        safetyUser.setUserEmail(originUser.getUserEmail());
        safetyUser.setUserPhone(originUser.getUserPhone());
        safetyUser.setUserProfile(originUser.getUserProfile());
        safetyUser.setUserIsAdmin(originUser.getUserIsAdmin());
        safetyUser.setUserCreatedTime(originUser.getUserCreatedTime());
        return safetyUser;
    }
}
