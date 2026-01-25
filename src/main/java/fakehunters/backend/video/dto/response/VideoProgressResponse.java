package fakehunters.backend.video.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VideoProgressResponse {
    @JsonProperty("progress")
    private Integer progress;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("detail")
    private String detail;
}