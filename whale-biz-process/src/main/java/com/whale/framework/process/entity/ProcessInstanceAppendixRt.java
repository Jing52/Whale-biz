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
 * @Date: 2022/12/6 10:56 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_instance_appendix_rt")
@ApiModel("流程定义")
public class ProcessInstanceAppendixRt extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 需要保存草稿,还未生成camunda流程实例
     */
    @Column(name = "local_proc_inst_id")
    @ApiModelProperty(value = "本地的流程实例ID")
    private String localProcInstId;

    @Column(name = "appendix_id")
    @ApiModelProperty(value = "附件ID")
    private String appendixId;

}
