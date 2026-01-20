package fakehunters.backend.image.service;

import fakehunters.backend.image.dto.response.ImageHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface ImageHistoryService {
    ImageHistoryResponse getHistory(List<UUID> jobUuids);
}
