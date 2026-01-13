-- 영상 분석 테이블
CREATE TABLE IF NOT EXISTS video_analyses (
    analysis_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- 영상 파일 테이블
CREATE TABLE IF NOT EXISTS video_files (
    file_id VARCHAR(255) PRIMARY KEY,
    original_filename VARCHAR(255),
    stored_filename VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT,
    duration_seconds DECIMAL(10, 2),
    resolution VARCHAR(50),
    format VARCHAR(10),
    fps DECIMAL(5, 2),
    uploaded_at TIMESTAMP,
    analysis_id VARCHAR(255),
    FOREIGN KEY (analysis_id) REFERENCES video_analyses(analysis_id)
);

-- 분석 결과 테이블
CREATE TABLE IF NOT EXISTS analysis_results (
    result_id VARCHAR(255) PRIMARY KEY,
    analysis_id VARCHAR(255),
    created_at TIMESTAMP,
    confidence_score DECIMAL(5, 4),
    is_deepfake BOOLEAN,
    model_version VARCHAR(50),
    processing_time_ms BIGINT,
    detected_techniques TEXT,
    summary TEXT,
    analyzed_at TIMESTAMP,
    FOREIGN KEY (analysis_id) REFERENCES video_analyses(analysis_id)
);

-- 프레임 분석 테이블
CREATE TABLE IF NOT EXISTS frame_analyses (
    frame_id VARCHAR(255) PRIMARY KEY,
    result_id VARCHAR(255),
    frame_number INTEGER,
    timestamp_seconds DECIMAL(10, 3),
    is_deepfake BOOLEAN,
    confidence_score DECIMAL(5, 4),
    anomaly_type VARCHAR(100),
    features TEXT,
    FOREIGN KEY (result_id) REFERENCES analysis_results(result_id)
);