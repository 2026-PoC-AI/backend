package fakehunters.backend;

import fakehunters.backend.text.domain.TextAnalysisHistory;
import java.util.UUID;
import fakehunters.backend.text.mapper.TextMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional // 테스트 후 DB에 데이터가 남지 않게 자동으로 롤백해줍니다.
class TextMapperTest {

    @Autowired
    private TextMapper textMapper;

    @Test
    @DisplayName("UUID 데이터가 정상적으로 DB에 저장되는지 확인")
    void insertAnalysisHistoryTest() {
        // Given: 테스트 데이터 준비
        TextAnalysisHistory history = new TextAnalysisHistory();
        history.setAnalysisId(UUID.randomUUID().toString());
        history.setRequestId(UUID.randomUUID());
        history.setInputText("테스트를 위한 문장입니다.");
        history.setFinalScore(95);
        history.setVerdict("SAFE");

        // When & Then: 실행 시 에러가 발생하지 않으면 성공
        textMapper.insertAnalysisHistory(history);

        System.out.println(" 매퍼 테스트 통과: UUID 데이터가 정상적으로 처리되었습니다.");
    }
}