-- A single night's sleep for a user.
--
--   * sleep_date                 -> the calendar date the sleep is logged for ("today")
--   * bed_time / wake_time       -> the "time in bed" interval as local timestamps
--   * total_time_in_bed_minutes  -> denormalized duration of the interval, stored so that
--                                   average queries stay simple and reads are cheap
--   * morning_feeling            -> how the user felt in the morning: BAD | OK | GOOD
--
-- A user can log at most one sleep per calendar date (one "last night" per day).
CREATE TABLE sleep_log (
    id                        UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id                   UUID        NOT NULL,
    sleep_date                DATE        NOT NULL,
    bed_time                  TIMESTAMP   NOT NULL,
    wake_time                 TIMESTAMP   NOT NULL,
    total_time_in_bed_minutes INTEGER     NOT NULL,
    morning_feeling           VARCHAR(8)  NOT NULL,
    created_at                TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_sleep_log PRIMARY KEY (id),
    CONSTRAINT fk_sleep_log_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_sleep_log_user_date UNIQUE (user_id, sleep_date),
    CONSTRAINT chk_sleep_log_feeling CHECK (morning_feeling IN ('BAD', 'OK', 'GOOD')),
    CONSTRAINT chk_sleep_log_interval CHECK (wake_time > bed_time),
    CONSTRAINT chk_sleep_log_minutes CHECK (total_time_in_bed_minutes > 0)
);

-- Supports both "fetch the latest log for a user" and the 30-day range scan.
CREATE INDEX idx_sleep_log_user_date ON sleep_log (user_id, sleep_date DESC);
