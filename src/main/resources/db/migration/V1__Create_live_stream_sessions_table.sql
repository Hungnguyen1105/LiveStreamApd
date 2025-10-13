-- Migration for LiveStreamSession table
-- Created: 2024-01-08

CREATE TABLE live_stream_sessions (
    id BIGSERIAL PRIMARY KEY,
    room_id VARCHAR(255) UNIQUE NOT NULL,
    host_id BIGINT NOT NULL,
    title VARCHAR(500),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT DEFAULT 0,
    max_viewers INTEGER DEFAULT 0,
    total_views INTEGER DEFAULT 0,
    total_gifts_value DECIMAL(15,2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'LIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster queries
CREATE INDEX idx_live_stream_sessions_host_id ON live_stream_sessions(host_id);
CREATE INDEX idx_live_stream_sessions_status ON live_stream_sessions(status);
CREATE INDEX idx_live_stream_sessions_start_time ON live_stream_sessions(start_time);
CREATE INDEX idx_live_stream_sessions_room_id ON live_stream_sessions(room_id);

-- Foreign key constraint (if users table exists)
-- ALTER TABLE live_stream_sessions 
-- ADD CONSTRAINT fk_live_stream_sessions_host_id 
-- FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE;