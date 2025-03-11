CREATE TABLE IF NOT EXISTS event_similarity
(
    id         BIGINT PRIMARY KEY,
    event_a_id BIGINT,
    event_b_id BIGINT,
    score      DOUBLE PRECISION,
    timestamp  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_actions
(
    id        BIGINT PRIMARY KEY,
    event_id  BIGINT,
    user_id   BIGINT,
    score     DOUBLE PRECISION,
    timestamp TIMESTAMP
);
