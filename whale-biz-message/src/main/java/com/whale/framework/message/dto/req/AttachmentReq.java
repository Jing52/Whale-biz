package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.message.api.dto.req
 * @Description: AttachmentReq
 * @Date: 2022/12/9 11:56 AM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "of")
public class AttachmentReq extends BaseRequest {

    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件路径
     */
    private String filePath;
}
