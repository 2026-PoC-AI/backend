package fakehunters.backend.image.service;

import fakehunters.backend.image.ai.AiImageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageAnalysisEventListener {

    private final AiImageClient aiImageClient;

    /**
     * DB 트랜잭션이 COMMIT 된 이후에만 호출됨
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ImageAnalysisCreatedEvent event) {
        log.info("AFTER_COMMIT AI image analysis call. jobUuid={}", event.jobUuid());

        aiImageClient.requestDeepfakeAnalysis(
                event.jobUuid(),
                event.s3Key()
        );
    }
}
