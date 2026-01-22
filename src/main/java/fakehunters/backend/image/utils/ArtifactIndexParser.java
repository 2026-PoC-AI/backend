package fakehunters.backend.image.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArtifactIndexParser {
    private static final Pattern P =
            Pattern.compile(".*_(\\d+)\\.(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE);

    private ArtifactIndexParser() {}

    public static Integer parseIndex(String s3Key) {
        if (s3Key == null) return null;
        Matcher m = P.matcher(s3Key);
        if (!m.matches()) return null;
        return Integer.valueOf(m.group(1));
    }
}
