package com.whale.framework.process.dto.rsp;

import com.magus.framework.core.dto.rsp.BaseRsp;
import com.magus.framework.system.api.dto.rsp.AppApplicationRsp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.dto.req
 * @Description:
 * @Date: 2022/12/9 4:23 PM
 */
@ApiModel
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDelegateRsp extends BaseRsp {

    @ApiModelProperty(value = "申请编号")
    String batchNo;

    @ApiModelProperty(value = "工作组Id")
    List<String> sysGroupIds;

    @ApiModelProperty(value = "委托工作组")
    List<AppApplicationRsp> sysApps;

    @ApiModelProperty(value = "受让人")
    private String assignee;

    @ApiModelProperty(value = "受让人名称")
    private String assigneeName;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty(value = "状态（是否启用,0=未启用;1=已启用）")
    private Boolean state;

    @ApiModelProperty(value = "创建时间）")
    private Date createTime;

}
