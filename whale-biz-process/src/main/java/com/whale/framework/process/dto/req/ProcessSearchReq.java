package com.whale.framework.process.dto.req;

import com.whale.framework.process.enums.ProcessModuleEnum;
import com.magus.framework.core.dto.req.BaseSearchReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ProjectName: magus-engine
 * See: com.magus.framework.camunda.dto.req
 * Description: ProcessInstanceSearchReq
 * Date: 2022/12/5 11:56 AM
 * @author Whale
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("ProcessSearchReq")
public class ProcessSearchReq extends BaseSearchReq {

    @ApiModelProperty(value = "开始时间")
    private Long beginTime;

    @ApiModelProperty(value = "结束时间")
    private Long endTime;

    @ApiModelProperty(value = "关键字")
    private String keyword;

    @ApiModelProperty(value = "分页来源")
    private ProcessModuleEnum source;

}
