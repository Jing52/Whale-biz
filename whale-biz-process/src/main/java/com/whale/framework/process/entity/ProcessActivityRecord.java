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
 * @Date: 2022/12/16 9:13 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_activity_record")
@ApiModel("流程动态记录表")
public class ProcessActivityRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "task_id")
    @ApiModelProperty(value = "任务实例id")
    private String taskId;

    @Column(name = "proc_inst_id")
    @ApiModelProperty(value = "流程实例ID")
    private String procInstId;

    @Column(name = "activity_status")
    @ApiModelProperty(value = "操作状态")
    private String activityStatus;

    @Column(name = "opinion")
    @ApiModelProperty(value = "意见")
    private String opinion;

    @Column(name = "appendix_ids")
    @ApiModelProperty(value = "附件ID（逗号分割）")
    private String appendixIds;

    @Column(name = "operator_id")
    @ApiModelProperty(value = "操作人ID")
    private String operatorId;

    @Column(name = "operator")
    @ApiModelProperty(value = "操作人")
    private String operator;

    @Column(name = "sort")
    @ApiModelProperty(value = "排序")
    private Integer sort;
}