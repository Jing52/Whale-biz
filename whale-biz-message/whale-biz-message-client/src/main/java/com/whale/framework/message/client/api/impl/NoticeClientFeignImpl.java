package com.whale.framework.message.client.api.impl;

import com.whale.framework.message.client.api.NoticeClient;
import com.whale.framework.message.client.hystrix.NoticeClientFallback;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.message.client.api
 * @Description:
 * @Date: 2023/7/7 4:08 PM
 */
@FeignClient(url = "${whale-biz-message:}", contextId = "NoticeRestApi", name = "magus-message", path = "/notice", fallback = NoticeClientFallback.class)
public interface NoticeClientFeignImpl extends NoticeClient {
}
