package com.dodo.dodoserver.global.common.constants;


public final class NotificationConstants {

    // Data Keys
    public static final String KEY_TYPE = "type";
    public static final String KEY_NEST_ID = "nestId";
    public static final String KEY_COMMENT_ID = "commentId";
    public static final String KEY_POSTCARD_ID = "postcardId";

    // Type Values
    public static final String TYPE_COMMENT = "COMMENT";
    public static final String TYPE_REPLY = "REPLY";
    public static final String TYPE_NEST_LIKE = "NEST_LIKE";
    public static final String TYPE_COMMENT_LIKE = "COMMENT_LIKE";
    public static final String TYPE_POSTCARD_EXCHANGED = "POSTCARD_EXCHANGED";
    public static final String TYPE_POSTCARD_LIKE = "POSTCARD_LIKE";

    // Message Templates
    public static final String TITLE_NEW_COMMENT = "둥지에 새 댓글이 달렸습니다!";
    public static final String BODY_NEW_COMMENT = "%s님이 댓글을 남겼습니다.";
    public static final String TITLE_NEW_REPLY = "내 댓글에 답글이 달렸습니다!";
    public static final String BODY_NEW_REPLY = "%s님이 답글을 남겼습니다.";
    public static final String TITLE_NEST_LIKE = "내 둥지에 좋아요가 달렸어요!";
    public static final String BODY_NEST_LIKE = "%s님이 회원님의 둥지를 좋아합니다.";
    public static final String TITLE_COMMENT_LIKE = "내 댓글에 좋아요가 달렸어요!";
    public static final String BODY_COMMENT_LIKE = "%s님이 회원님의 댓글을 좋아합니다.";
    public static final String TITLE_POSTCARD_EXCHANGED = "나의 엽서가 교환되었습니다!";
    public static final String BODY_POSTCARD_EXCHANGED = "누군가 %s둥지에서 당신의 엽서를 가져갔어요!";
    public static final String TITLE_POSTCARD_LIKE = "내 엽서에 리액션이 달렸어요!";
    public static final String BODY_POSTCARD_LIKE = "%s님이 회원님의 엽서에 '%s'라고 반응했습니다.";

    private NotificationConstants() {
    }
}
