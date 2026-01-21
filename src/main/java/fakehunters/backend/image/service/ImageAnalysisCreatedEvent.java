package fakehunters.backend.image.service;

import java.util.UUID;

/**
 * Image analysis job이 생성된 직후 발생하는 이벤트
 * (TX commit 이후 AI 호출용)
 */
public record ImageAnalysisCreatedEvent(
        UUID jobUuid,
        String s3Key
) {
}
