CREATE TABLE recipes
(
    id             VARCHAR(255) NOT NULL,
    user_entity_id BIGINT,
    title          VARCHAR(255),
    description    VARCHAR(255),
    ingredients    VARCHAR(255),
    directions     VARCHAR(255),
    cuisine        VARCHAR(255),
    status         SMALLINT,
    rating         SMALLINT,
    CONSTRAINT pk_recipes PRIMARY KEY (id)
);