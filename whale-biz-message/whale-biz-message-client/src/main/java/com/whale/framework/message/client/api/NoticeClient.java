package com.whale.framework.message.client.api;

import com.whale.framework.common.domain.response.BaseResponse;
import com.whale.framework.message.client.domain.request.NoticeRequest;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.message.client.api
 * @Description:
 * @Date: 2023/7/7 4:08 PM
 */
public interface NoticeClient {

    BaseResponse notice(@RequestBody NoticeRequest noticeReq);
}
