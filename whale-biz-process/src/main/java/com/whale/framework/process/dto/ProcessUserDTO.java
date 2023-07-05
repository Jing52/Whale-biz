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
public class ProcessUserDTO {

    @ApiModelProperty(value = "流程实例ID")
    String operator;

    @ApiModelProperty(value = "任务实例ID")
    String operatorId;
}
