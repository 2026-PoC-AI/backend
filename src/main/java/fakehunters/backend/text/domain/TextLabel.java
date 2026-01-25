package fakehunters.backend.text.domain;

public enum TextLabel {
    FAKE, TRUE, REAL;

    public static TextLabel from(String v) {
        if (v == null) return null;
        return TextLabel.valueOf(v.trim().toUpperCase());
    }

    // 운영 권장: REAL을 TRUE로 정규화
    public static TextLabel normalize(String v) {
        TextLabel t = from(v);
        return t == REAL ? TRUE : t;
    }
}
