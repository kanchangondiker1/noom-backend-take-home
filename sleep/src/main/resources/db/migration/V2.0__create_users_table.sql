-- Users of the sleep logger. Authentication/authorization is out of scope for this
-- assignment, but the domain still needs the concept of a user so that every sleep
-- log is owned by exactly one person and averages can be scoped per user.
CREATE TABLE users (
    id         UUID         NOT NULL,
    username   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);
