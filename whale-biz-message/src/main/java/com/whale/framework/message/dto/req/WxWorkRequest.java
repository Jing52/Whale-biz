package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.message.api.dto.req
 * @Description:
 * @Date: 2023/5/25 3:45 PM
 */
@Data
@Schema
public class WxWorkRequest extends BaseRequest {

    @NotBlank
    @Schema(description = "webHook地址")
    private String webHook;

    @NotBlank
    @Schema(description = "消息类型")
    private String msgType;

    @Schema(description = "文本")
    private WeWorkTextRequest text;

    @Schema(description = "markdown")
    private WeWorkMarkDownRequest markdown;

    @Schema(description = "图片")
    private WeWorkImageRequest image;

    @Schema(description = "卡片")
    private WeWorkNewsRequest news;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkTextRequest {

        @Schema(description = "正文")
        private String content;

        @Schema(description = "@用户")
        private List<String> mentioned_list;

        @Schema(description = "@手机号")
        private List<String> mentioned_mobile_list;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkMarkDownRequest {

        @Schema(description = "正文")
        private String content;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkImageRequest {

        private String media_id;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkNewsRequest {

        private List<WeWorkArticleRequest> articles;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkArticleRequest {

        private String title;

        private String description;

        private String url;

        private String picurl;

    }
}
