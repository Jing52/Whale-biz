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
 * @Description: 流程定义
 * @Author: Whale
 * @Date: 2022/12/5 2:49 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_definition")
@ApiModel("流程定义")
public class ProcessDefinition extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @Column(name = "proc_def_name")
    @ApiModelProperty(value = "流程定义name")
    private String procDefName;

    @Column(name = "proc_def_key")
    @ApiModelProperty(value = "流程定义key")
    private String procDefKey;

    @ApiModelProperty(value = "流程定义版本")
    @Column(name = "version")
    private Integer version;

    @ApiModelProperty(value = "状态（是否启用,0=未启用;1=已启用）")
    @Column(name = "state")
    private Boolean state;

    @ApiModelProperty(value = "流程发起后允许撤回（是否启用,0=未启用;1=已启用）")
    @Column(name = "withdraw_flag")
    private Boolean withdrawFlag;

    @ApiModelProperty(value = "流程发起后允许评论（是否启用,0=未启用;1=已启用）")
    @Column(name = "comment_flag")
    private Boolean commentFlag;

    @ApiModelProperty(value = "允许查看流程动态和流程图（是否启用,0=未启用;1=已启用）")
    @Column(name = "view_flag")
    private Boolean viewFlag;

    @ApiModelProperty(value = "自动同意规则(CLOSE:不启用；START_USER：审批人为发起人；NEIGHBOR：审批人与上一节点处理人相同；PARTICIPATION：审批人处理过该流程(默认选择不启用)")
    @Column(name = "complete_rule")
    private String completeRule;

    @ApiModelProperty(value = "驳回规则(FROM_START_NODE:从头开始审批；FROM_REJECT_NODE：从驳回节点审批(默认选择从头开始审批)")
    @Column(name = "reject_rule")
    private String rejectRule;

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
