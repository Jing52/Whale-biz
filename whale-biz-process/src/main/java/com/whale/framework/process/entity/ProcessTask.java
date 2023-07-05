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
@Table(name = "process_task")
@ApiModel("流程节点实例")
public class ProcessTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "task_id")
    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @Column(name = "proc_inst_id")
    @ApiModelProperty(value = "流程实例ID")
    private String procInstId;

    @Column(name = "node_key")
    @ApiModelProperty(value = "节点定义key")
    private String nodeKey;

    @Column(name = "business_table_name")
    @ApiModelProperty(value = "业务数据归属的数据模型名称")
    private String businessTableName;

    @Column(name = "business_id")
    @ApiModelProperty(value = "业务数据Id")
    private String businessId;
}
