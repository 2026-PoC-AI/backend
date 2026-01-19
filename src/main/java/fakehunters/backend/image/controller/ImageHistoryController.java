package fakehunters.backend.image.controller;

import fakehunters.backend.image.dto.request.ImageHistoryRequest;
import fakehunters.backend.image.dto.response.ImageHistoryResponse;
import fakehunters.backend.image.service.ImageHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageHistoryController {
    private final ImageHistoryService historyService;

    @PostMapping("/history")
    public ImageHistoryResponse getHistory(
            @RequestBody ImageHistoryRequest request
    ) {
        return historyService.getHistory(request.getJobUuids());
    }
}
