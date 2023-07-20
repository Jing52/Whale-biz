package com.whale.framework.user.service.impl;

import com.whale.framework.message.client.api.NoticeClient;
import com.whale.framework.message.client.domain.request.NoticeRequest;
import com.whale.framework.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.user.service.impl
 * @Description:
 * @Date: 2023/7/10 10:30 AM
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    NoticeClient noticeClient;

    @Override
    public void getUser(String userId) {
        noticeClient.notice(new NoticeRequest());
    }
}
