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
public class ProcessIdentityLinkDTO {

    @ApiModelProperty(value = "ID")
    String id;

    @ApiModelProperty(value = "版本")
    String rev;

    @ApiModelProperty(value = "用户组ID")
    String groupId;

    @ApiModelProperty(value = "类型")
    String type;

    @ApiModelProperty(value = "用户ID")
    String userId;

    @ApiModelProperty(value = "任务ID")
    String taskId;

    @ApiModelProperty(value = "流程定义ID")
    String procDefId;

    @ApiModelProperty(value = "租户ID")
    String tenantId;
}
