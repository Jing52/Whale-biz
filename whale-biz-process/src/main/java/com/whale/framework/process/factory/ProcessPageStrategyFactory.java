package com.whale.framework.process.factory;

import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.strategy.module.IProcessStrategy;
import com.magus.framework.core.exception.MagusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service.factory
 * @Description: camunda流程工厂
 * @Date: 2022/12/7 11:44 AM
 */
@Component
@Slf4j
public class ProcessPageStrategyFactory implements ApplicationContextAware {

    private static final Map<String, IProcessStrategy> map = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IProcessStrategy> result = applicationContext.getBeansOfType(IProcessStrategy.class);
        result.forEach((k, v) -> map.put(v.getStrategyKey(), v));
    }

    public <T extends IProcessStrategy> T getStrategy(String strategyKey) {
        IProcessStrategy strategy = map.get(IProcessStrategy.CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + strategyKey);
        if (strategy == null) {
            log.error("===================> 获取策略失败  strategyKey： {}", IProcessStrategy.CAMUNDA_PROCESS_STRATEGY_KEY_PREFIX + strategyKey);
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_STRATEGY_NOT_FOUND);
        }
        return (T) strategy;
    }
}
