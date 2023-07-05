package com.whale.framework.process.dto.rsp;

import com.magus.framework.core.dto.rsp.BaseRsp;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.dto.rsp
 * @Description:
 * @Date: 2023/5/11 2:34 PM
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("ProcessApplyPageRsp")
public class ProcessDefinitionTemplateRsp extends BaseRsp {

    /**
     * 流程定义id
     */
    private String procDefId;

    /**
     * 流程定义名称
     */
    private String procDefName;

    /**
     * 流程定义key
     */
    private String procDefKey;

    /**
     * 类型
     */
    private String type;

    /**
     * 行业分类
     */
    private String industryCategory;

    /**
     * 场景分类
     */
    private String sceneCategory;

    /**
     * 终端分类
     */
    private String terminalCategory;

    /**
     * 版本
     */
    private Integer version;

    /**
     * 封面
     */
    private String coverPage;

    /**
     * bpmn模型
     */
    private String bpmnModel;
}
