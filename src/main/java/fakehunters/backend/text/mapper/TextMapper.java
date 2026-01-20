package fakehunters.backend.text.mapper;

import fakehunters.backend.text.domain.TextAnalysisHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.fasterxml.jackson.databind.JsonNode;

@Mapper
public interface TextMapper {
    // 1. 기본 이력 저장 (F-01)
    void insertAnalysisHistory(TextAnalysisHistory history);

    // 2. 내적 분석 결과 저장
    void insertInternalAnalysis(@Param("analysisId") String analysisId,
                                @Param("linguisticScore") Integer linguisticScore,
                                @Param("biasScore") Double biasScore,
                                @Param("sensationalScore") Double sensationalScore);

    // 3. 외신 기사 저장 (반복문 처리 예정)
    void insertExternalArticle(@Param("analysisId") String analysisId,
                               @Param("title") String title,
                               @Param("url") String url,
                               @Param("publisher") String publisher,
                               @Param("similarityScore") Double similarityScore);

    // 4. 메타데이터 저장 (JSONB 활용)
    void insertAnalysisMetadata(@Param("analysisId") String analysisId,
                                @Param("contradictions") JsonNode contradictions,
                                @Param("gdeltImpact") JsonNode gdeltImpact);
}