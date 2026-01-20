package fakehunters.backend.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ImageHistoryResponse {
    private List<ImageHistoryItemResponse> items;
}
