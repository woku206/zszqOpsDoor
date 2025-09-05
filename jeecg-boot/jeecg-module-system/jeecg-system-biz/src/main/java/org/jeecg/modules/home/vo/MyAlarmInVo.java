package org.jeecg.modules.home.vo;

import java.util.Date;


import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

// import java.io.Serializable;

import lombok.Data;

/**
 *
 *Description: 我的告警vo
 * @author: zengyun
 */
@Data
public class MyAlarmInVo{


    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 登录账号
     */
    @Excel(name = "登录账号", width = 15)
    private String username;

    /**
     * 告警内容
     */
    @Excel(name = "告警内容", width = 500)
    private String content;



     /**
     * 告警时间
     */
    @Excel(name = "告警时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alarmTime;

    // /**
    //  * 生日
    //  */
    // @Excel(name = "生日", width = 15, format = "yyyy-MM-dd")
    // @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    // @DateTimeFormat(pattern = "yyyy-MM-dd")
    // private Date birthday;

    // /**
    //  * 性别（1：男 2：女）
    //  */
    // @Excel(name = "性别", width = 15,dicCode="sex")
    // @Dict(dicCode = "sex")
    // private Integer sex;

    /**
     * 主机
     */
    @Excel(name = "主机", width = 15)
    private String host;

    /**
     * 系统名称
     */
    @Excel(name = "系统名称", width = 15)
    private String businessName;


    /**
     * 告警状态(1：待处理  2：处理中 ）
     */
    @Excel(name = "告警状态", width = 15,dicCode="alarm_status")
    @Dict(dicCode = "alarm_status")
    private Integer alarmStatus;

    /**
     * 告警级别(1：通知 2：次要 3:一般 4:严重 5:紧急 ）
     */
    @Excel(name = "告警级别", width = 15,dicCode="alarm_level")
    @Dict(dicCode = "alarm_level")
    private Integer alarmLevel;


}
