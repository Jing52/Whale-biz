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
 * @Date: 2022/12/8 10:58 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_comment")
@ApiModel("流程实例评论表")
public class ProcessComment extends BaseEntity {

    @ApiModelProperty(value = "流程实例ID")
    @Column(name = "proc_inst_id")
    private String procInstId;

    @ApiModelProperty(value = "评论")
    @Column(name = "comment")
    private String comment;

    @ApiModelProperty(value = "评论")
    @Column(name = "create_user_name")
    private String createUserName;
}
