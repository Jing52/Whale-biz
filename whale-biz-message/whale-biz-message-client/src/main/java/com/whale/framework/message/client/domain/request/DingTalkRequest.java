package com.whale.framework.message.client.domain.request;

import com.whale.framework.common.domain.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.magus.framework.message.api.dto.req
 * @Description:
 * @Date: 2023/5/25 3:48 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DingTalkRequest extends BaseRequest {

    @NotBlank
    private String webHook;

    @NotBlank
    private String msgType;

    private DingTalkText text;

    private DingTalkMarkDown markdown;

    private DingTalkActionCard actionCard;

    private DingTalkFeedCard feedCard;

    private DingTalkAt at;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkText {

        private String content;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkMarkDown {

        private String title;

        private String text;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkActionCard {

        private String title;

        private String text;

        private String hideAvatar;

        private String btnOrientation;

        private List<DingTalkActionCardBtn> btns;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkActionCardBtn {

        private String title;

        private String actionURL;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkFeedCard {

        private List<DingTalkFeedCardLink> links;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkFeedCardLink {

        private String title;

        private String messageURL;

        private String picURL;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DingTalkAt {

        private String[] atMobiles;

        private boolean isAtAll;

    }

}
