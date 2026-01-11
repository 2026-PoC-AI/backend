package fakehunters.backend.video.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    private Long id;
    private String filename;
    private String description;
    private Integer duration; // 초 단위
    private Boolean isDeepfake;
}