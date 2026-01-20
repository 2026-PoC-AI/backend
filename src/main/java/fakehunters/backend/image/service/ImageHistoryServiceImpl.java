package fakehunters.backend.image.service;


import fakehunters.backend.image.dto.response.ImageHistoryItemResponse;
import fakehunters.backend.image.dto.response.ImageHistoryResponse;
import fakehunters.backend.image.mapper.ImageAnalysisHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageHistoryServiceImpl implements ImageHistoryService {

    private final ImageAnalysisHistoryMapper historyMapper;

    @Override
    @Transactional(readOnly = true)
    public ImageHistoryResponse getHistory(List<UUID> jobUuids) {

        if (jobUuids == null || jobUuids.isEmpty()) {
            return new ImageHistoryResponse(Collections.emptyList());
        }

        List<ImageHistoryItemResponse> items =
                historyMapper.findHistoryByJobUuids(jobUuids);

        return new ImageHistoryResponse(items);
    }
}
