package com.whale.framework.user.api.impl;

import com.whale.framework.user.api.UserClient;
import com.whale.framework.user.hystrix.UserClientFallback;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.user.client.api
 * @Description:
 * @Date: 2023/7/7 4:08 PM
 */
@FeignClient(url = "${whale-biz-message:}", contextId = "NoticeRestApi", name = "magus-message", path = "/notice", fallback = UserClientFallback.class)
public interface UserClientFeignImpl extends UserClient {
}
