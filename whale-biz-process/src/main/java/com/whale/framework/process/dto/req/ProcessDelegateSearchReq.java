package com.whale.framework.process.dto.req;

import com.magus.framework.core.dto.req.BaseSearchReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.dto.req
 * @Description:
 * @Date: 2022/12/9 4:23 PM
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("ProcessDelegateSearchReq")
public class ProcessDelegateSearchReq extends BaseSearchReq {


    @ApiModelProperty(value = "批次号")
    String batchNo;

    @ApiModelProperty(value = "功能组名称")
    String sysGroupName;
}
