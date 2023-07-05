package com.whale.framework.process.dto.req;

import com.whale.framework.process.enums.ProcessTemplateEnum;
import com.magus.framework.core.dto.req.BaseReq;
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
 * @Description: 流程定义模板
 * @Author: Whale
 * @Date: 2022/12/5 2:49 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("流程定义模板")
public class ProcessDefinitionTemplateReq extends BaseReq {

    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @ApiModelProperty(value = "流程定义name")
    private String procDefName;

    @ApiModelProperty(value = "流程定义key")
    private String procDefKey;

    @ApiModelProperty(value = "类型")
    private ProcessTemplateEnum type;

    @ApiModelProperty(value = "行业分类")
    private String industryCategory;

    @ApiModelProperty(value = "场景分类")
    private String sceneCategory;

    @ApiModelProperty(value = "终端分类")
    private String terminal_category;

    @Column(name = "版本号")
    private Integer version;

    @ApiModelProperty(value = "封面")
    private String coverPage;

}
