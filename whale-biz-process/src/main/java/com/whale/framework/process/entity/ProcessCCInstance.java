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
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.entity
 * @Description:
 * @Author: Whale
 * @Date: 2022/12/6 11:44 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_cc_instance")
@ApiModel("流程传阅实例")
public class ProcessCCInstance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "task_id")
    @ApiModelProperty(value = "任务实例id")
    private String taskId;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @Column(name = "proc_inst_id")
    @ApiModelProperty(value = "流程实例id")
    private String procInstId;

    @Column(name = "user_id")
    @ApiModelProperty(value = "传阅的人")
    private String userId;

    @ApiModelProperty(value = "状态（是否启用,0=失效;1=有效）")
    @Column(name = "state")
    private Integer state;
}
