package com.whale.framework.user.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.user.client.hystrix
 * @Description:
 * @Date: 2023/7/7 4:12 PM
 */
@Component
public class UserClientFallback {

    private final Logger logger = LoggerFactory.getLogger(getClass());

}
