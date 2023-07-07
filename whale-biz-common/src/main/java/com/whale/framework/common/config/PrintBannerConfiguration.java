package com.whale.framework.common.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.common.config
 * @Description:
 * @Date: 2023/7/7 3:26 PM
 */
@Component
@Slf4j
public class PrintBannerConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${test.version}")
    private String version;

    private String bannerName = " :: Test :: ";

    public static final String[] BANNER = {
            " ____  ____  ____  ____ ",
            "(_  _)(  __)/ ___)(_  _)",
            "  )(   ) _) \\___ \\  )(",
            " (__) (____)(____/ (__) "
    };

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("start finish");
        changeAppender("BANNER");
        printBanner();
        changeAppender("NORMAL");
    }

    private void printBanner() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        for (String s : BANNER) {
            printStream.println(s);
        }

        version = (version != null) ? " (v" + version + ")" : "";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < 24 - (version.length() + bannerName.length())) {
            padding.append(" ");
        }

        printStream.println(AnsiOutput.toString(AnsiColor.GREEN, bannerName, AnsiColor.DEFAULT, padding.toString(),
                AnsiStyle.FAINT, version));
        try {
            log.info(baos.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void changeAppender(String logType) {
        // 获取log配置文件上下文
        LoggerContext logger = (LoggerContext) LoggerFactory.getILoggerFactory();
        // 获取根节点root
        Logger loggerForRoot = logger.getLogger("root");
        Logger loggerForRolling = logger.getLogger("loggerForRolling");
        // root节点移除appender组件
        loggerForRoot.detachAppender("CONSOLE");
        loggerForRoot.detachAppender("ROLLING");
        loggerForRoot.detachAppender("BANNER");

        RollingFileAppender rollingAppender = (RollingFileAppender) loggerForRolling.getAppender("ROLLING");
        // 由于打印banner和打印普通日志不一样，则要根据不同的模式切换root节点的appender
        switch (logType) {
            case "BANNER": {
                Logger loggerForBanner = logger.getLogger("loggerForBanner");
                ConsoleAppender bannerAppender = (ConsoleAppender) loggerForBanner.getAppender("BANNER");
                // 这一步是为了输出的日志文件和控制台统一都使用banner的encoder
                rollingAppender.setEncoder(bannerAppender.getEncoder());
                // 把appender放回去root就能激活使用
                loggerForRoot.addAppender(rollingAppender);
                loggerForRoot.addAppender(bannerAppender);
            }
            break;
            case "NORMAL": {
                Logger loggerForConsole = logger.getLogger("loggerForConsole");
                ConsoleAppender consoleAppender = (ConsoleAppender) loggerForConsole.getAppender("CONSOLE");
                rollingAppender.setEncoder(consoleAppender.getEncoder());
                loggerForRoot.addAppender(rollingAppender);
                loggerForRoot.addAppender(consoleAppender);
            }
            break;
        }

    }

}