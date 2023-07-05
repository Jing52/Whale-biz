package com.whale.framework.message.properties;

import com.whale.framework.message.enums.DingTalkMsgTypeEnum;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static com.whale.framework.message.enums.DingTalkMsgTypeEnum.TEXT;

/**
 * @Description: 钉钉机器人配置
 * @Author Whale
 * @Date: 2023/3/30 10:37 AM
 */
@Data
@ConditionalOnProperty(prefix = ExceptionNoticeProperties.PREFIX, name = "enable", havingValue = "true")
public class DingTalkProperties {

    public static final String PREFIX = "notice.exception.ding-talk";

    /**
     * 开关
     */
    private Boolean enable;

    /**
     * 钉钉机器人webHook地址
     */
    private String webHook;

    /**
     * 发送消息时被@的钉钉用户手机号
     */
    private String[] atMobiles;

    /**
     * 发送消息时被@的钉钉用户手机号
     */
    private Boolean isAtAll = false;

    /**
     * 消息类型 暂只支持text和markdown
     */
    private DingTalkMsgTypeEnum msgType = TEXT;
}
