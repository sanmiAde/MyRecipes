-- Convert recipes.status from SMALLINT (ordinal) to VARCHAR with enum names
-- Map current ordinals based on enum order in code: 0=SHARED, 1=DRAFT
ALTER TABLE recipes
    ALTER COLUMN status TYPE VARCHAR(20)
        USING CASE status
            WHEN 0 THEN 'SHARED'
            WHEN 1 THEN 'DRAFT'
            ELSE 'DRAFT'
        END;

-- Expand text fields for real content sizes
ALTER TABLE recipes
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN ingredients TYPE TEXT,
    ALTER COLUMN directions TYPE TEXT,
    ALTER COLUMN cuisine TYPE VARCHAR(100);
