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
 * @Description: 流程实例
 * @Author: Whale
 * @Date: 2022/12/5 2:49 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_node_action")
@ApiModel("流程节点操作权限")
public class ProcessNodeAction extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @Column(name = "node_key")
    @ApiModelProperty(value = "节点定义key")
    private String nodeKey;

    @Column(name = "action_code")
    @ApiModelProperty(value = "操作编码")
    private String actionCode;

    @Column(name = "alias_name")
    @ApiModelProperty(value = "别名")
    private String aliasName;

    @Column(name = "enable")
    @ApiModelProperty(value = "是否授权")
    private Boolean enable;

}
