package com.robbie.linebot.infra.support;

import com.robbie.linebot.infra.provider.GmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ErrorReporter {
    private final GmailProvider gmailProvider;

    public void report(String context, Exception e) {
        log.error("系統錯誤 [{}]: {}", context, e.getMessage());
        gmailProvider.sendError(context, e);
    }
}
