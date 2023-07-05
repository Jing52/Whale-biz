package com.whale.framework.process.entity;

import com.magus.framework.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

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
@Table(name = "process_instance")
@ApiModel("流程定义")
public class ProcessInstance extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "process_no")
    @ApiModelProperty(value = "编号")
    private String processNo;

    @Column(name = "title")
    @ApiModelProperty(value = "流程实例标题")
    private String title;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @Column(name = "proc_inst_id")
    @ApiModelProperty(value = "流程实例id")
    private String procInstId;

    @Column(name = "page_id")
    @ApiModelProperty(value = "页面标识编码")
    private String pageId;

    @Column(name = "page_uri")
    @ApiModelProperty(value = "页面标识uri")
    private String pageUri;

    @ApiModelProperty(value = "被驳回的节点")
    @Column(name = "reject_node_key")
    private String rejectNodeKey;

    @ApiModelProperty(value = "发起人")
    @Column(name = "start_user_id")
    private String startUserId;

    @ApiModelProperty(value = "发起人")
    @Column(name = "start_user_name")
    private String startUserName;

    @ApiModelProperty(value = "状态")
    @Column(name = "status")
    private String status;

    @Column(name = "business_table_name")
    @ApiModelProperty(value = "业务表名称")
    private String businessTableName;

    @ApiModelProperty(value = "业务数据ID")
    @Column(name = "business_id")
    private String businessId;

    @CreatedDate
    @Column(name = "start_time", columnDefinition = "timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间'")
    @ApiModelProperty("发起时间")
    private Date startTime;

}
