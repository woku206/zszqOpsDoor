package org.jeecg.modules.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 防火墙策略申请表实体
 */
@Data
@TableName("ticket")
@Schema(description = "工单申请表")
public class FirewallTicket implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    /**
     * 工单类型
     */
    @Schema(description = "工单类型")
    private String ticketType;

    /**
     * 创建用户
     */
    @Schema(description = "创建用户")
    private String createUser;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 基本信息(JSON格式)
     */
    @Schema(description = "基本信息(JSON格式)")
    private String baseInfo;

    /**
     * 工单信息(JSON格式) - 包含策略配置和生效时间
     */
    @Schema(description = "工单信息(JSON格式)")
    private String ticketInfo;

    /**
     * 申请信息
     */
    @Schema(description = "申请信息")
    private String applyInfo;

    /**
     * 附件路径
     */
    @Schema(description = "附件路径")
    private String attachmentPath;

    /**
     * ITIL附件UUID
     */
    @Schema(description = "ITIL附件UUID")
    private String itilAttachmentUuid;

     /**
     * ITIL流程ID
     */
    @Schema(description = "ITIL流程ID")
    private String itilId;

    /**
     * ITIL流程实例ID
     */
    @Schema(description = "ITIL流程实例ID")
    private String processInstanceId;

    /**
     * ITIL流程Key
     */
    @Schema(description = "ITIL流程Key")
    private String processKey;
}

