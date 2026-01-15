package fakehunters.backend.audio.dto.external;

// 요청 DTO (Spring -> FastAPI)
public record AiAudioRequest(
        String audio_s3_key // FastAPI schema와 변수명 일치 (snake_case)
) {}