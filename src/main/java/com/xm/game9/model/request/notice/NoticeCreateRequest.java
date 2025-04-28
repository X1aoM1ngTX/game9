package com.xm.gamehub.model.request.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 公告创建请求体
 *
 * @author X1aoM1ngTX
 */
@Schema(description = "公告创建请求")
@Data
public class NoticeCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 654116216864910L;

    @Schema(description = "公告标题")
    private String noticeTitle;
    @Schema(description = "公告内容")
    private String noticeContent;
    @Schema(description = "公告类型")
    private Integer noticeType;
    @Schema(description = "公告状态（创建时为草稿状态）")
    private Integer noticeStatus;
    @Schema(description = "公告是否删除")
    private Integer noticeIsDelete;
    @Schema(description = "公告结束时间")
    private Date noticeExpireTime;
    @Schema(description = "公告创建者ID")
    private Long noticeCreatorId;

}
