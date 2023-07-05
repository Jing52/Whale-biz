package com.whale.framework.process.factory;

import com.whale.framework.process.result.ProcessResultEnum;
import com.whale.framework.process.strategy.action.IProcessActionStrategy;
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
public class ProcessActionStrategyFactory implements ApplicationContextAware {

    private static final Map<String, IProcessActionStrategy> map = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IProcessActionStrategy> result = applicationContext.getBeansOfType(IProcessActionStrategy.class);
        result.forEach((k, v) -> map.put(v.getStrategyKey(), v));
    }

    public <T extends IProcessActionStrategy> T getStrategy(String strategyKey) {
        IProcessActionStrategy strategy = map.get(IProcessActionStrategy.CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + strategyKey);
        if (strategy == null) {
            log.error("===================> 获取策略失败  strategyKey： {}", IProcessActionStrategy.CAMUNDA_PROCESS_ACTION_STRATEGY_KEY_PREFIX + strategyKey);
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_STRATEGY_NOT_FOUND);
        }
        return (T) strategy;
    }
}
