package com.whale.framework.message.properties;

import com.whale.framework.message.enums.WxWorkMsgTypeEnum;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.whale.framework.message.enums.WxWorkMsgTypeEnum.TEXT;

/**
 * @Description: 企业微信机器人配置
 * @Author Whale
 * @Date: 2023/3/30 12:36 AM
 */
@Data
@Component
@ConditionalOnProperty(prefix = ExceptionNoticeProperties.PREFIX, name = "enable", havingValue = "true")
public class WxWorkProperties {

    public static final String PREFIX = "notice.exception.ding-talk";

    /**
     * 开关
     */
    private Boolean enable;

    /**
     * userid的列表，提醒群中的指定成员(@某个成员)，@all表示提醒所有人，如果开发者获取不到userid，可以使用atPhones
     */
    private String[] atUserIds;

    /**
     * 手机号列表，提醒手机号对应的群成员(@某个成员)，@all表示提醒所有人
     */
    private String[] atPhones;

    /**
     * 企业微信机器人webHook地址
     */
    private String webHook;

    /**
     * 消息类型
     */
    private WxWorkMsgTypeEnum msgType = TEXT;
}
