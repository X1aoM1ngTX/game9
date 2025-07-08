package com.xm.game9.common;

import lombok.Getter;

/**
 * 错误码
 *
 * @author X1aoM1ngTX
 */
@Getter
public enum ErrorCode {
    // 通用错误码 (0-9999)
    SUCCESS(0, "ok", "成功"),
    PARAMS_ERROR(40000, "请求参数错误", "请求参数错误"),
    NULL_ERROR(40001, "请求参数为空", "请求参数为空"),
    NOT_LOGIN(40100, "未登录", "未登录"),
    NO_AUTH(40101, "用户无权限", "用户无权限"),
    SYSTEM_ERROR(50000, "系统内部异常", "系统内部异常"),
    NOT_FOUND_ERROR(50001, "未发现", "未发现"),
    OPERATION_ERROR(50003, "操作失败", "操作失败"),

    // 用户系统相关错误码 (50100-50199)
    USER_NOT_FOUND(50100, "用户不存在", "找不到该用户"),
    USER_ALREADY_EXIST(50101, "用户已存在", "该用户已存在"),
    USER_PASSWORD_ERROR(50102, "密码错误", "用户密码错误"),
    USER_ACCOUNT_EXPIRED(50103, "账号已过期", "用户账号已过期"),
    USER_CREDENTIALS_ERROR(50104, "凭证错误", "用户凭证错误"),
    USER_ACCOUNT_DISABLE(50105, "账号已禁用", "用户账号已被禁用"),
    USER_ACCOUNT_LOCKED(50106, "账号已锁定", "用户账号已被锁定"),
    USER_ACCOUNT_NOT_EXIST(50107, "账号不存在", "用户账号不存在"),
    USER_ACCOUNT_ALREADY_EXIST(50108, "账号已存在", "用户账号已存在"),
    USER_ACCOUNT_USE_BY_OTHERS(50109, "账号下线", "用户账号在其他地方登录"),
    USER_AVATAR_UPLOAD_ERROR(50110, "头像上传失败", "用户头像上传失败"),
    USER_EMAIL_ALREADY_EXIST(50111, "邮箱已存在", "该邮箱已被注册"),
    USER_EMAIL_NOT_EXIST(50112, "邮箱不存在", "该邮箱未注册"),
    USER_EMAIL_CODE_ERROR(50113, "验证码错误", "邮箱验证码错误或已过期"),
    USER_EMAIL_SEND_ERROR(50114, "验证码发送失败", "邮箱验证码发送失败"),
    USER_PHONE_ALREADY_EXIST(50115, "手机号已存在", "该手机号已被注册"),
    USER_PHONE_NOT_EXIST(50116, "手机号不存在", "该手机号未注册"),
    USER_PHONE_CODE_ERROR(50117, "验证码错误", "手机验证码错误或已过期"),
    USER_PHONE_SEND_ERROR(50118, "验证码发送失败", "手机验证码发送失败"),
    USER_SIGN_IN_ERROR(50119, "签到失败", "用户签到失败"),

    // 游戏系统相关错误码 (50200-50299)
    GAME_NOT_FOUND(50200, "游戏不存在", "找不到该游戏"),
    GAME_ALREADY_EXIST(50201, "游戏已存在", "该游戏已存在"),
    GAME_STOCK_ERROR(50202, "库存不足", "游戏库存不足"),
    GAME_PRICE_ERROR(50203, "价格错误", "游戏价格错误"),
    GAME_DISCOUNT_ERROR(50204, "折扣错误", "游戏折扣错误"),
    GAME_SALE_ERROR(50205, "促销错误", "游戏促销错误"),
    GAME_REVIEW_ERROR(50206, "评价错误", "游戏评价错误"),
    GAME_REVIEW_NOT_FOUND(50207, "评价不存在", "找不到该评价"),
    GAME_REVIEW_ALREADY_EXIST(50208, "评价已存在", "该游戏已评价"),
    GAME_REVIEW_NO_AUTH(50209, "无权评价", "需要先购买游戏才能评价"),
    GAME_LIBRARY_ERROR(50210, "游戏库错误", "游戏库操作错误"),
    GAME_LIBRARY_NOT_FOUND(50211, "游戏不存在", "游戏库中不存在该游戏"),
    GAME_LIBRARY_ALREADY_EXIST(50212, "游戏已存在", "游戏库中已存在该游戏"),

    // 好友系统相关错误码 (50300-50399)
    FRIEND_REQUEST_EXIST(50300, "好友请求已存在", "已经发送过好友请求"),
    FRIEND_ALREADY_EXIST(50301, "好友关系已存在", "已经是好友关系"),
    FRIEND_REQUEST_NOT_FOUND(50302, "好友请求不存在", "找不到相关的好友请求"),
    FRIEND_NOT_FOUND(50303, "好友关系不存在", "找不到相关的好友关系"),
    FRIEND_CANNOT_ADD_SELF(50304, "不能添加自己为好友", "不能添加自己为好友"),
    FRIEND_REQUEST_HANDLED(50305, "好友请求已处理", "该好友请求已经被处理过"),
    FRIEND_REQUEST_REJECTED(50306, "好友请求被拒绝", "好友请求被拒绝"),
    FRIEND_LIMIT_EXCEEDED(50307, "好友数量超限", "好友数量已达到上限"),
    FRIEND_OPERATION_ERROR(50308, "好友操作失败", "好友相关操作失败"),
    FRIEND_GROUP_EXISTS(50309, "分组名称已存在", "好友分组名称已存在"),
    FRIEND_GROUP_NOT_FOUND(50310, "分组不存在", "找不到相关的好友分组"),
    FRIEND_GROUP_NAME_ERROR(50311, "分组名称错误", "好友分组名称不合法"),
    FRIEND_GROUP_LIMIT_EXCEEDED(50312, "分组数量超限", "好友分组数量已达到上限"),
    FRIEND_GROUP_DELETE_ERROR(50313, "分组删除失败", "好友分组删除失败"),
    FRIEND_ONLINE_STATUS_ERROR(50314, "在线状态更新失败", "用户在线状态更新失败"),

    // 资讯系统相关错误码 (50400-50499)
    NEWS_NOT_FOUND(50400, "资讯不存在", "找不到该资讯"),
    NEWS_ALREADY_EXIST(50401, "资讯已存在", "该资讯已存在"),
    NEWS_TITLE_TOO_LONG(50402, "标题过长", "资讯标题长度不能超过255"),
    NEWS_SUMMARY_TOO_LONG(50403, "摘要过长", "资讯摘要长度不能超过500"),
    NEWS_CONTENT_ERROR(50404, "内容错误", "资讯内容错误"),
    NEWS_STATUS_ERROR(50405, "状态错误", "资讯状态错误"),
    NEWS_PUBLISH_ERROR(50406, "发布失败", "资讯发布失败"),
    NEWS_DRAFT_ERROR(50407, "草稿失败", "资讯草稿失败"),
    NEWS_NO_AUTH(50408, "无权限操作", "没有操作该资讯的权限"),

    // 文件系统相关错误码 (50500-50599)
    FILE_UPLOAD_ERROR(50500, "文件上传失败", "文件上传失败"),
    FILE_DELETE_ERROR(50501, "文件删除失败", "文件删除失败"),
    FILE_NOT_FOUND(50502, "文件不存在", "找不到该文件"),
    FILE_SIZE_ERROR(50503, "文件大小错误", "文件大小超出限制"),
    FILE_TYPE_ERROR(50504, "文件类型错误", "不支持的文件类型"),
    FILE_NAME_ERROR(50505, "文件名错误", "文件名不合法"),
    FILE_PATH_ERROR(50506, "文件路径错误", "文件路径不合法"),
    FILE_PERMISSION_ERROR(50507, "文件权限错误", "没有文件操作权限"),
    FILE_FORMAT_ERROR(50508, "文件格式错误", "文件格式不正确"),

    // Redis相关错误码 (50600-50699)
    REDIS_CONNECTION_ERROR(50600, "Redis连接错误", "Redis连接失败"),
    REDIS_CONFIG_ERROR(50601, "Redis配置错误", "Redis配置错误"),
    REDIS_OPERATION_ERROR(50602, "Redis操作错误", "Redis操作失败"),
    REDIS_KEY_NOT_FOUND(50603, "键不存在", "Redis中不存在该键"),
    REDIS_KEY_EXPIRED(50604, "键已过期", "Redis中的键已过期"),
    REDIS_KEY_TYPE_ERROR(50605, "键类型错误", "Redis中的键类型错误"),
    REDIS_MEMORY_ERROR(50606, "内存不足", "Redis内存不足"),
    REDIS_PERSISTENCE_ERROR(50607, "持久化错误", "Redis持久化失败"),
    REDIS_LOCK_ERROR(50608, "分布式锁错误", "Redis分布式锁操作失败");

    private final int errorCode;
    private final String message;
    private final String description;

    ErrorCode(int errorCode, String message, String description) {
        this.errorCode = errorCode;
        this.message = message;
        this.description = description;
    }
}
