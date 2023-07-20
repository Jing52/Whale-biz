package com.whale.framework.message.client.api.impl;

import com.whale.framework.common.domain.response.BaseResponse;
import com.whale.framework.message.client.api.NoticeClient;
import com.whale.framework.message.client.domain.request.NoticeRequest;
import com.whale.framework.message.client.hystrix.NoticeClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.message.client.api
 * @Description:
 * @Date: 2023/7/7 4:08 PM
 */
@FeignClient(url = "${whale-message:}", contextId = "NoticeRestApi", name = "whale-message", path = "/notice", fallback = NoticeClientFallback.class)
public interface NoticeClientFeignImpl extends NoticeClient {

    @Override
    @PostMapping()
    BaseResponse notice(@RequestBody NoticeRequest noticeReq);
}
