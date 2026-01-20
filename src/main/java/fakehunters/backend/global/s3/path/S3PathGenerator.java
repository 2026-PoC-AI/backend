package fakehunters.backend.global.s3.path;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class S3PathGenerator {

    /**
     * Fake Hunters S3 path 규칙
     * domain/stage/YYYY/MM/DD/{uuid}.{ext}
     * ex) image/inputs/2026/01/12/550e8400-e29b-41d4-a716-446655440000.png
     */
    public String generate(
            String domain,
            String stage,
            String extension
    ) {
        validate(domain, stage, extension);

        LocalDate now = LocalDate.now();

        return String.format(
                "%s/%s/%04d/%02d/%02d/%s.%s",
                normalize(domain),
                normalize(stage),
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                UUID.randomUUID(),
                normalizeExt(extension)
        );
    }

    private void validate(String domain, String stage, String extension) {
        if (!StringUtils.hasText(domain)) {
            throw new IllegalArgumentException("domain must not be empty");
        }
        if (!StringUtils.hasText(stage)) {
            throw new IllegalArgumentException("stage must not be empty");
        }
        if (!StringUtils.hasText(extension)) {
            throw new IllegalArgumentException("extension must not be empty");
        }
    }

    private String normalize(String value) {
        return value.trim()
                .toLowerCase()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
    }

    private String normalizeExt(String ext) {
        String e = ext.trim().toLowerCase();
        return e.startsWith(".") ? e.substring(1) : e;
    }
}
