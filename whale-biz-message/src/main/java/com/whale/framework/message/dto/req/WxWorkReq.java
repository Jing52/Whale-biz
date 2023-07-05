package com.whale.framework.message.dto.req;

import com.whale.framework.common.dto.request.BaseRequest;
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
public class WxWorkReq extends BaseRequest {

    @NotBlank
    private String webHook;

    @NotBlank
    private String msgType;

    private WeWorkTextReq text;

    private WeWorkMarkDownReq markdown;

    private WeWorkImageReq image;

    private WeWorkNewsReq news;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkTextReq {

        private String content;

        private List<String> mentioned_list;

        private List<String> mentioned_mobile_list;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkMarkDownReq {

        private String content;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkImageReq {

        private String media_id;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkNewsReq {

        private List<WeWorkArticle> articles;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class WeWorkArticle {

        private String title;

        private String description;

        private String url;

        private String picurl;

    }
}
