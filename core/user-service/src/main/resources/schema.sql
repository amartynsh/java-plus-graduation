create TABLE IF NOT EXISTS users
(
    id    bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  varchar(255)                            NOT NULL,
    email varchar(255)                            NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT users_email_unique UNIQUE (email)
);