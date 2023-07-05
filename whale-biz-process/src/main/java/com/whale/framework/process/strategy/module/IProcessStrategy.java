package com.whale.framework.process.strategy.module;

import com.magus.framework.camunda.api.dto.req.ProcessSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessInstanceRsp;
import com.whale.framework.process.strategy.base.BaseStrategy;
import org.springframework.data.domain.PageImpl;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.base
 * @Description:
 * @Date: 2022/12/7 11:45 AM
 */
public interface IProcessStrategy extends BaseStrategy {

    public final static String CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX = IProcessStrategy.class.getSimpleName() + KEY_CONCAT;

    Integer count();

    PageImpl<ProcessInstanceRsp> page(ProcessSearchReq req);
}
