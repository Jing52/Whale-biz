package com.whale.framework.process.job;

import com.whale.framework.process.entity.ProcessDelegate;
import com.whale.framework.process.service.ProcessDelegateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.job
 * @Description: 定时任务查找已经过期但是未失效的委托（每天凌晨2点执行）
 * @Date: 2023/4/4 2:11 PM
 */
@Slf4j
@Component
@EnableScheduling
public class ProcessDelegateCancelJob {
    @Autowired
    private ProcessDelegateService processDelegateService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void job() {
        log.info("=========================> ProcessDelegateCancelJob start job");
        try {
            cancelDelegate();
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        log.info("=========================> ProcessDelegateCancelJob end");
    }

    /**
     * 取消委托
     */
    private void cancelDelegate() {
        List<ProcessDelegate> delegates = processDelegateService.findExpiredDelegate();
        if (CollectionUtils.isNotEmpty(delegates)) {
            log.info("=========================> Cancel delegate start");
            processDelegateService.cancelDelegate(delegates);
        }
    }

}
