package com.whale.framework.message.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Description: 邮件附件
 * @Author Whale
 * @Date: 2023/3/22 10:30 AM
 */
@Data
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "of")
public class Attachment {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;
}


