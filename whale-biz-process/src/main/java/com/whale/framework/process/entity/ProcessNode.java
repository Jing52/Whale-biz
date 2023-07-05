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
@Table(name = "process_node")
@ApiModel("流程节点")
public class ProcessNode extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "node_key")
    @ApiModelProperty(value = "节点定义key")
    private String nodeKey;

    @Column(name = "node_name")
    @ApiModelProperty(value = "节点定义名称D")
    private String nodeName;

    @Column(name = "node_type")
    @ApiModelProperty(value = "节点类型")
    private String nodeType;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义ID")
    private String procDefId;
    
    @Column(name = "proc_def_name")
    @ApiModelProperty(value = "流程定义名称")
    private String procDefName;

    @Column(name = "assignee")
    @ApiModelProperty(value = "task处理人")
    private String assignee;

    @Column(name = "candidate_users")
    @ApiModelProperty(value = "task候选人参数")
    private String candidateUsers;

    @Column(name = "candidate_groups")
    @ApiModelProperty(value = "task候选人组参数")
    private String candidateGroups;

    @Column(name = "applicant")
    @ApiModelProperty(value = "是否是申请人节点")
    private Boolean applicant;

    @Column(name = "cc_flag")
    @ApiModelProperty(value = "是否启用抄送")
    private Boolean ccFlag;

    @Column(name = "opinion_flag")
    @ApiModelProperty(value = "是否启用意见输入框")
    private Boolean opinionFlag;

    @Column(name = "signature_flag")
    @ApiModelProperty(value = "是否启用手写签名")
    private Boolean signatureFlag;

    @Column(name = "page_flag")
    @ApiModelProperty(value = "是否是内部表单：默认true")
    private Boolean pageFlag;

    @Column(name = "page_id")
    @ApiModelProperty(value = "页面id")
    private String pageId;

    @Column(name = "page_uri")
    @ApiModelProperty(value = "页面uri")
    private String pageUri;

}
