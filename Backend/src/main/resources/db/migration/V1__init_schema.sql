-- 1. Tabela USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela USER_PROFILES
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(255),
    gender VARCHAR(50),
    birth_date DATE,
    weight_kg NUMERIC(5, 2),
    height_cm NUMERIC(5, 2),
    activity_level VARCHAR(100),
    goal VARCHAR(255),
    daily_kcal_goal INT,
    avatar_url VARCHAR(512)
);

-- 3. Tabela FOOD_PRODUCTS
CREATE TABLE food_products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    kcal_per_100g NUMERIC(7, 2) NOT NULL DEFAULT 0.00,
    protein_g NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    fat_g NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    carbs_g NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    brand VARCHAR(255)
);

-- 4. Tabela RECIPES
CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(512),
    prep_time_min INT NOT NULL DEFAULT 0,
    servings INT NOT NULL DEFAULT 1,
    kcal_per_serving NUMERIC(7, 2) DEFAULT 0.00,
    protein_g NUMERIC(6, 2) DEFAULT 0.00,
    fat_g NUMERIC(6, 2) DEFAULT 0.00,
    carbs_g NUMERIC(6, 2) DEFAULT 0.00,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabela RECIPE_TAGS
CREATE TABLE recipe_tags (
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (recipe_id, tag)
);

-- 6. Tabela RECIPE_INGREDIENTS
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES food_products(id) ON DELETE CASCADE,
    quantity_g NUMERIC(8, 2) NOT NULL,
    unit VARCHAR(50) DEFAULT 'g',
    sort_order INT NOT NULL DEFAULT 0
);

-- 7. Tabela DIARY_ENTRIES
CREATE TABLE diary_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entry_date DATE NOT NULL,
    meal_type VARCHAR(100) NOT NULL,
    product_id BIGINT REFERENCES food_products(id) ON DELETE SET NULL,
    recipe_id BIGINT, 
    custom_name VARCHAR(255),
    quantity_g NUMERIC(8, 2) NOT NULL,
    kcal NUMERIC(8, 2) NOT NULL DEFAULT 0.00,
    protein_g NUMERIC(6, 2) DEFAULT 0.00,
    fat_g NUMERIC(6, 2) DEFAULT 0.00,
    carbs_g NUMERIC(6, 2) DEFAULT 0.00,
    photo_path VARCHAR(512),
    note TEXT,
    synced BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Tabela FAVORITE_RECIPES
CREATE TABLE favorite_recipes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_recipe UNIQUE (user_id, recipe_id)
);

-- 9. Tabela WORKOUTS
CREATE TABLE workouts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_date DATE NOT NULL,
    activity_type VARCHAR(255) NOT NULL,
    duration_min INT NOT NULL,
    kcal_burned INT NOT NULL,
    distance_km NUMERIC(6, 2),
    avg_heart_rate INT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);