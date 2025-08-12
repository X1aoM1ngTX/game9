package com.xm.game9.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steam URL解析工具类
 * 用于从Steam游戏商店URL中提取游戏AppID
 */
public class SteamUrlParser {

    /**
     * Steam URL正则表达式模式
     * 匹配以下格式：
     * - https://store.steampowered.com/app/1172470/Apex_Legends/
     * - https://store.steampowered.com/app/1172470/
     * - https://steamcommunity.com/app/1172470/
     */
    private static final Pattern STEAM_URL_PATTERN = Pattern.compile(
            "https?://(?:store\\.steampowered|steamcommunity)\\.com/app/(\\d+)/?.*"
    );

    /**
     * 从Steam URL中提取游戏AppID
     *
     * @param steamUrl Steam游戏URL
     * @return 游戏AppID，如果URL格式不正确则返回null
     */
    public static String extractAppId(String steamUrl) {
        if (StringUtils.isBlank(steamUrl)) {
            return null;
        }

        Matcher matcher = STEAM_URL_PATTERN.matcher(steamUrl.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 验证是否为有效的Steam游戏URL
     *
     * @param steamUrl 待验证的URL
     * @return 是否为有效的Steam游戏URL
     */
    public static boolean isValidSteamUrl(String steamUrl) {
        if (StringUtils.isBlank(steamUrl)) {
            return false;
        }
        return STEAM_URL_PATTERN.matcher(steamUrl.trim()).matches();
    }

    /**
     * 标准化Steam URL
     * 确保URL以正确的格式返回
     *
     * @param steamUrl 原始URL
     * @return 标准化后的URL，如果无效则返回null
     */
    public static String normalizeSteamUrl(String steamUrl) {
        if (StringUtils.isBlank(steamUrl)) {
            return null;
        }

        String appId = extractAppId(steamUrl);
        if (appId == null) {
            return null;
        }

        return "https://store.steampowered.com/app/" + appId + "/";
    }
}