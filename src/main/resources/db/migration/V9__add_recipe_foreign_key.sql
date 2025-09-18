ALTER TABLE ratings
    ADD recipe_id BIGINT;

ALTER TABLE ratings
    ADD CONSTRAINT FK_RATINGS_ON_RECIPE FOREIGN KEY (recipe_id) REFERENCES recipes (id);

ALTER TABLE ratings
    DROP COLUMN data;

ALTER TABLE ratings
    ADD data VARCHAR(255);