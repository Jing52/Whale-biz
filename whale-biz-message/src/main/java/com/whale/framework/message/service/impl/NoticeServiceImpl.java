package com.whale.framework.message.service.impl;

import com.whale.framework.common.dto.exception.BizServiceException;
import com.whale.framework.common.dto.response.ResponseCodeEnum;
import com.whale.framework.common.utils.MapUtil;
import com.whale.framework.message.dto.req.DingTalkRequest;
import com.whale.framework.message.dto.req.MailRequest;
import com.whale.framework.message.dto.req.WxWorkRequest;
import com.whale.framework.message.enums.DingTalkMsgTypeEnum;
import com.whale.framework.message.enums.WxWorkMsgTypeEnum;
import com.whale.framework.message.service.NoticeService;
import com.whale.framework.message.service.MailService;
import com.whale.framework.message.utils.DingTalkUtil;
import com.whale.framework.message.utils.WxWorkUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Whale
 * @ProjectName: magus-engine
 * @See: com.whale.framework.message.service.impl
 * @Description:
 * @Date: 2023/5/25 3:25 PM
 */
@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    MailService mailService;

    @Override
    public String wxWorkNotice(WxWorkRequest wxWork) {
        if (Objects.isNull(wxWork) || StringUtils.isBlank(wxWork.getMsgType())) {
            throw new BizServiceException(ResponseCodeEnum.ILLEGAL_ARGUMENT.getCode(), ResponseCodeEnum.ILLEGAL_ARGUMENT.getFormatMsg("消息类型不能为空"));
        }
        String result = null;
        try {
            String msgType = wxWork.getMsgType();
            String webHook = wxWork.getWebHook();
            switch (WxWorkMsgTypeEnum.valueOf(msgType)) {
                case TEXT -> {
                    if (Objects.isNull(wxWork.getText())) {
                        return result;
                    }
                    Map<String, Object> text = MapUtil.covertObj2Map(wxWork.getText());
                    result = WxWorkUtil.sendTextMsg(webHook, text);
                }
                case MARKDOWN -> {
                    if (Objects.isNull(wxWork.getMarkdown())) {
                        return result;
                    }
                    Map<String, Object> markdown = MapUtil.covertObj2Map(wxWork.getMarkdown());
                    result = WxWorkUtil.sendMarkDownTextMsg(webHook, markdown);
                }
                case IMAGE -> {
                    if (Objects.isNull(wxWork.getImage())) {
                        return result;
                    }
                    Map<String, Object> image = MapUtil.covertObj2Map(wxWork.getImage());
                    result = WxWorkUtil.sendImageMsg(webHook, image);
                }
                case NEWS -> {
                    if (Objects.isNull(wxWork.getNews()) || Objects.isNull(wxWork.getNews().getArticles())) {
                        return result;
                    }
                    Map<String, List<Map<String, Object>>> news = new HashMap<>();

                    List<WxWorkRequest.WeWorkArticleRequest> articles = wxWork.getNews().getArticles();
                    List<Map<String, Object>> articleList = articles.stream().map(link -> {
                        Map<String, Object> map = new HashMap<>();
                        BeanUtils.copyProperties(link, map);
                        return map;
                    }).collect(Collectors.toList());
                    news.put("articles", articleList);
                    result = WxWorkUtil.sendNewsMsg(webHook, news);
                }
                default -> {
                    throw new BizServiceException(ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getCode(), ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getFormatMsg("该企业微信类型不支持！"));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public String dingTalkNotice(DingTalkRequest dingTalk) {
        if (Objects.isNull(dingTalk) || StringUtils.isBlank(dingTalk.getWebHook()) || StringUtils.isBlank(dingTalk.getMsgType())) {
            throw new BizServiceException(ResponseCodeEnum.ILLEGAL_ARGUMENT.getCode(), ResponseCodeEnum.ILLEGAL_ARGUMENT.getFormatMsg("钉钉的webHook地址和消息类型不能为空"));
        }
        String result = null;
        try {
            String webHook = dingTalk.getWebHook();
            String msgType = dingTalk.getMsgType();
            DingTalkRequest.DingTalkAt dingTalkAt = dingTalk.getAt();
            Map<String, Object> at = MapUtil.covertObj2Map(dingTalkAt);
            switch (DingTalkMsgTypeEnum.valueOf(msgType)) {
                case TEXT -> {
                    if (Objects.isNull(dingTalk.getText())) {
                        return result;
                    }
                    Map<String, Object> text = MapUtil.covertObj2Map(dingTalk.getText());
                    result = DingTalkUtil.sendTextMsg(webHook, text, at);
                }
                case MARKDOWN -> {
                    if (Objects.isNull(dingTalk.getMarkdown())) {
                        return result;
                    }
                    Map<String, Object> markdown = MapUtil.covertObj2Map(dingTalk.getMarkdown());
                    result = DingTalkUtil.sendMarkDownMsg(webHook, markdown, at);
                }
                case ACTION_CARD -> {
                    if (Objects.isNull(dingTalk.getActionCard())) {
                        return result;
                    }
                    Map<String, Object> actionCard = MapUtil.covertObj2Map(dingTalk.getActionCard());
                    result = DingTalkUtil.sendActionCardMsg(webHook, actionCard, at);
                }
                case FEED_CARD -> {
                    if (Objects.isNull(dingTalk.getFeedCard()) || Objects.isNull(dingTalk.getFeedCard().getLinks())) {
                        return result;
                    }
                    Map<String, List<Map<String, Object>>> feedCard = new HashMap<>();

                    List<DingTalkRequest.DingTalkFeedCardLink> dingTalkFeedCardLinks = dingTalk.getFeedCard().getLinks();
                    List<Map<String, Object>> links = dingTalkFeedCardLinks.stream().map(link -> {
                        Map<String, Object> map = new HashMap<>();
                        BeanUtils.copyProperties(link, map);
                        return map;
                    }).collect(Collectors.toList());
                    feedCard.put("links", links);

                    result = DingTalkUtil.sendFeedCardMsg(webHook, feedCard, at);
                }
                default -> {
                    throw new BizServiceException(ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getCode(), ResponseCodeEnum.BIZ_COMMON_EXCEPTION.getFormatMsg("该企业微信类型不支持！"));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Boolean mailNotice(MailRequest email) {
        return mailService.sendMail(email);
    }
}
