-- Add indexes to improve query performance
-- Index on recipes(user_entity_id) for filtering by owner
CREATE INDEX IF NOT EXISTS idx_recipes_user_entity_id ON recipes(user_entity_id);

-- Index on ratings(recipe_id) to speed up rating lookups by recipe
CREATE INDEX IF NOT EXISTS idx_ratings_recipe_id ON ratings(recipe_id);

-- Optional: case-insensitive search on cuisine (PostgreSQL functional index)
-- Uncomment if you are on PostgreSQL and use LOWER(r.cuisine) in queries
-- CREATE INDEX IF NOT EXISTS idx_recipes_lower_cuisine ON recipes (LOWER(cuisine));
