package com.dodo.dodoserver.global.common.constants;


public final class NotificationConstants {

    // Data Keys
    public static final String KEY_TYPE = "type";
    public static final String KEY_NEST_ID = "nestId";
    public static final String KEY_COMMENT_ID = "commentId";

    // Type Values
    public static final String TYPE_COMMENT = "COMMENT";
    public static final String TYPE_REPLY = "REPLY";

    // Message Templates
    public static final String TITLE_NEW_COMMENT = "둥지에 새 댓글이 달렸습니다!";
    public static final String BODY_NEW_COMMENT = "%s님이 댓글을 남겼습니다.";
    public static final String TITLE_NEW_REPLY = "내 댓글에 답글이 달렸습니다!";
    public static final String BODY_NEW_REPLY = "%s님이 답글을 남겼습니다.";

    private NotificationConstants() {
    }
}
