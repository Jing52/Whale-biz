package com.whale.framework.process.dto.rsp;

import com.magus.framework.core.dto.rsp.BaseRsp;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.dto.rsp
 * @Description:
 * @Date: 2022/12/13 1:42 PM
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("ProcessApplyInfoRsp")
public class ProcessApplyInfoRsp extends BaseRsp {

    private String dataId;

    private String localProInstId;

    private String proInstId;

    private String tableName;

    private String tableDesc;

    private Object datas;

    private Date createTime;
}
