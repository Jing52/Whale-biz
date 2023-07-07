package com.whale.framework.message.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @Description: 邮箱配置
 * @Author Whale
 * @Date: 2023/3/30 10:36 AM
 */
@Data
public class MailProperties {

    public static final String PREFIX = "notice.exception.mail";

    /**
     * 是否开启
     */
    private Boolean enable;

    /**
     * 发送人
     */
    private String from;

    /**
     * 接收人，可多选
     */
    private String[] to;

    /**
     * 抄送人，可多选
     */
    private String[] cc;

}
