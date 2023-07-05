package com.whale.framework.process.dto.req;

import com.magus.framework.core.dto.req.BaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.dto.req
 * @Description:
 * @Date: 2022/12/9 4:23 PM
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("ProcessDelegateReq")
public class ProcessDelegateReq extends BaseReq {

    @ApiModelProperty(value = "工作组Id")
    List<String> sysGroupIds;

    @ApiModelProperty(value = "受让人")
    private String assignee;

    @ApiModelProperty(value = "委托批次")
    private String batchNo;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "状态（是否启用,0=未启用;1=已启用）")
    private Boolean state;

    public boolean checkParam() {
        if (CollectionUtils.isEmpty(this.sysGroupIds) || StringUtils.isBlank(this.assignee) || Objects.isNull(startTime) || (Objects.nonNull(endTime) && endTime.before(new Date()))) {
            return true;
        }
        return false;
    }
}
