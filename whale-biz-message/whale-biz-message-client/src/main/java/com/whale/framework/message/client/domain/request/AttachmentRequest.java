package com.whale.framework.message.client.domain.request;

import com.whale.framework.common.dto.request.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema
public class AttachmentRequest extends BaseRequest {

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;
}
