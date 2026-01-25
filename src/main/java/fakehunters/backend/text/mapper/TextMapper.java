package fakehunters.backend.text.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface TextMapper {

    int insertTextAnalysis(
            @Param("requestText") String requestText,
            @Param("evidenceK") int evidenceK,
            @Param("includeReferences") boolean includeReferences,
            @Param("requestId") UUID requestId,
            @Param("label") String label,
            @Param("score") double score
    );

    long selectLastInsertId();

    int insertEvidence(
            @Param("analysisId") long analysisId,
            @Param("text") String text,
            @Param("score") double score,
            @Param("ord") int ord
    );

    int insertHighlight(
            @Param("analysisId") long analysisId,
            @Param("startIdx") int startIdx,
            @Param("endIdx") int endIdx,
            @Param("text") String text,
            @Param("weight") double weight,
            @Param("ord") int ord
    );

    int insertReference(
            @Param("analysisId") long analysisId,
            @Param("title") String title,
            @Param("url") String url,
            @Param("snippet") String snippet,
            @Param("ord") int ord
    );
}
