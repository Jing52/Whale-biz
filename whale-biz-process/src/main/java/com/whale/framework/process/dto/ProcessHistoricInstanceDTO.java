package com.whale.framework.process.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.system.dto
 * @Description:
 * @Date: 2023/1/14 10:22 AM
 */
@Data
public class ProcessHistoricInstanceDTO {

    @ApiModelProperty(value = "流程实例ID")
    String procInstId;

    @ApiModelProperty(value = "任务实例ID")
    String taskId;

    @ApiModelProperty(value = "节点key", position = 30)
    private String nodeKey;

    @ApiModelProperty(value = "认领标识", position = 30)
    private Boolean claimFlag;

    @ApiModelProperty(value = "委托标识", position = 30)
    private Boolean delegateFlag;
}
