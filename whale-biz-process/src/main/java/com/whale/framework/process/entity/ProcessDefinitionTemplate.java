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
 * @Description: 流程定义模板
 * @Author: Whale
 * @Date: 2022/12/5 2:49 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_definition_template")
@ApiModel("流程定义模板")
public class ProcessDefinitionTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "proc_def_id")
    @ApiModelProperty(value = "流程定义id")
    private String procDefId;

    @Column(name = "proc_def_name")
    @ApiModelProperty(value = "流程定义名称")
    private String procDefName;

    @Column(name = "proc_def_key")
    @ApiModelProperty(value = "流程定义key")
    private String procDefKey;

    @Column(name = "type")
    @ApiModelProperty(value = "类型")
    private String type;

    @Column(name = "industry_category", length = 50)
    @ApiModelProperty(value = "行业分类")
    private String industryCategory;

    @Column(name = "scene_category", length = 50)
    @ApiModelProperty(value = "场景分类")
    private String sceneCategory;

    @Column(name = "terminal_category", length = 50)
    @ApiModelProperty(value = "终端分类")
    private String terminalCategory;

    @ApiModelProperty(value = "流程定义版本")
    @Column(name = "version")
    private Integer version;

    @Column(name = "cover_page")
    @ApiModelProperty(value = "封面")
    private String coverPage;

    @Column(name = "comp_id")
    @ApiModelProperty(value = "公司id")
    private String compId;

}
