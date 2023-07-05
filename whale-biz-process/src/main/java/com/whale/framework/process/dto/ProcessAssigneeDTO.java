package com.whale.framework.process.dto;

import com.whale.framework.process.enums.ProcessAssigneeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.system.dto
 * @Description:
 * @Date: 2023/1/14 10:22 AM
 */
@Data
public class ProcessAssigneeDTO<T> {

    @ApiModelProperty(value = "类型")
    ProcessAssigneeTypeEnum type;

    @ApiModelProperty(value = "行为")
    List<ActorDTO> actorList;

    @Data
    public static class ActorDTO {

        @ApiModelProperty(value = "id")
        String id;

        @ApiModelProperty(value = "名称")
        String name;

        @ApiModelProperty(value = "职务")
        List<ActorDTO> duty;
    }
}
