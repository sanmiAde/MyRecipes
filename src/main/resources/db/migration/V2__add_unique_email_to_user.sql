ALTER TABLE "user"
    ADD email VARCHAR(255);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_username UNIQUE (username);