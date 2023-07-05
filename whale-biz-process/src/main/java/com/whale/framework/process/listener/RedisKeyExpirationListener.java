package com.whale.framework.process.listener;

import com.whale.framework.process.entity.ProcessDelegate;
import com.whale.framework.process.service.ProcessDelegateService;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.listener
 * @Description: Key过期监听器
 * @Date: 2023/4/4 2:11 PM
 */
@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 监听到到过期消息
     *
     * @param message key
     * @param pattern 消息事件
     * @return void
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        ProcessDelegateService processDelegateService = SpringUtils.getBean(ProcessDelegateService.class);
        String expiredKey = message.toString();
        log.info("=========================> 监听到key过期：{}", expiredKey);
        if (Objects.nonNull(expiredKey) && expiredKey.startsWith(ProcessDelegateService.DELEGATE_CONFIGURATION)) {
            log.info("=========================> 监听到委托配置过期：{}", expiredKey);
            // 查询批次号的委托尚未失效
            List<ProcessDelegate> delegates = processDelegateService.expiredByBatchNo(expiredKey);
            log.info("=========================> 查询批次号尚未失效的委托：{}", JsonUtils.toJson(delegates));
            if (CollectionUtils.isNotEmpty(delegates)) {
                // 取消委托
                processDelegateService.cancelDelegate(delegates);
            }
        }
    }
}

