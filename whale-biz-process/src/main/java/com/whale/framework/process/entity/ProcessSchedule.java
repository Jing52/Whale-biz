package com.whale.framework.process.entity;

import com.magus.framework.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.entity
 * @Description:
 * @Date: 2023/5/10 5:38 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_schedule")
@ApiModel("流程节点定时配置")
public class ProcessSchedule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义ID")
    private String procDefId;

    @Column(name = "node_key")
    @ApiModelProperty(value = "节点定义key")
    private String nodeKey;

    @Column(name = "time_type")
    @ApiModelProperty(value = "节点处理截止时间类型")
    private String timeType;

    @Column(name = "after_time")
    @ApiModelProperty(value = "流程到达节点后的时间")
    private Integer afterTime;

    @Column(name = "after_unit")
    @ApiModelProperty(value = "流程到达节点后的时间单位")
    private String afterUnit;

    @Column(name = "column_name")
    @ApiModelProperty(value = "字段")
    private String columnName;

    @Column(name = "timeout_type")
    @ApiModelProperty(value = "超时后处理规则类型")
    private String timeoutType;

    @Column(name = "handle_type")
    @ApiModelProperty(value = "自动处理方式（自动提醒||自动提交||自动回退）")
    private String handleType;

    @Column(name = "auto_type")
    @ApiModelProperty(value = "在节点处理截止时间当时||在节点处理截止时间之前||在节点处理截止时间之后")
    private String autoType;

    @Column(name = "offset_time")
    @ApiModelProperty(value = "时间（在节点处理截止时间之前||在节点处理截止时间之后 才有该配置）")
    private Integer offsetTime;

    @Column(name = "offset_unit")
    @ApiModelProperty(value = "时间单位（在节点处理截止时间之前||在节点处理截止时间之后 才有该配置）")
    private String offsetUnit;

    @Column(name = "remind_message")
    @ApiModelProperty(value = "提醒文案（自动提醒才会有该配置）")
    private String remindMessage;

    @Column(name = "reverse_type")
    @ApiModelProperty(value = "回退的节点类型（自动回退才会有该配置）")
    private String reverseType;
}
