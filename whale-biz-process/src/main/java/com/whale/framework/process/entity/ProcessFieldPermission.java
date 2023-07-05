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
 * @Date: 2023/5/23 2:27 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_field_permission")
@ApiModel("流程字段权限")
public class ProcessFieldPermission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "page_id")
    @ApiModelProperty(value = "页面id")
    private String pageId;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @Column(name = "node_key")
    @ApiModelProperty(value = "节点key")
    private String nodeKey;

    @Column(name = "field_name")
    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @Column(name = "permission")
    @ApiModelProperty(value = "权限")
    private String permission;
}
