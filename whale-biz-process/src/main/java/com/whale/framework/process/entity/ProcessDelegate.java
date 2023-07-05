package com.whale.framework.process.entity;

import com.magus.framework.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

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
@Table(name = "process_delegate")
@ApiModel("流程委托配置")
public class ProcessDelegate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "sys_group_id")
    @ApiModelProperty(value = "功能组id")
    private String sysGroupId;

    @Column(name = "sys_group_name")
    @ApiModelProperty(value = "功能组名称")
    private String sysGroupName;

    @Column(name = "sys_app_id")
    @ApiModelProperty(value = "应用id")
    private String sysAppId;

    @Column(name = "owner")
    @ApiModelProperty(value = "委托人")
    private String owner;

    @Column(name = "assignee")
    @ApiModelProperty(value = "受让人")
    private String assignee;

    @Column(name = "batch_no")
    @ApiModelProperty(value = "委托批次")
    private String batchNo;

    @ApiModelProperty(value = "开始时间")
    @Column(name = "start_time")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @Column(name = "end_time")
    private Date endTime;

    @ApiModelProperty(value = "备注")
    @Column(name = "remark")
    private String remark;

    @ApiModelProperty(value = "状态（是否启用,0=失效;1=有效）")
    @Column(name = "state")
    private Boolean state;
}
