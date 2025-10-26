CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY, -- ID
    name TEXT NOT NULL,                     -- Название
    category TEXT NOT NULL,                 -- Категория
    cost_price REAL NOT NULL,               -- Себестоимость
    retail_price REAL NOT NULL,             -- Розничная цена
    unit TEXT NOT NULL,                     -- Единица измерения
    supplier TEXT NOT NULL,                 -- Поставщик
    stock INTEGER DEFAULT 0,                -- Остаток
    minimum INTEGER DEFAULT 0,              -- Минимальный остаток
    status TEXT NOT NULL                    -- Статус
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,                                                               -- ID
    sale_date TIMESTAMP DEFAULT NOW(),                                                      -- Дата
    product_id INTEGER NOT NULL,                                                            -- ID Товара
    product_name TEXT NOT NULL,                                                             -- Название товара
    quantity INTEGER NOT NULL CHECK (quantity > 0),                                         -- Кол-во проданного товара
    unit_price NUMERIC(12, 2) NOT NULL,                                                     -- Розничная цена за штуку
    total_price NUMERIC(12, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,          -- Сумма продажи
    cost_price NUMERIC(12, 2) NOT NULL,                                                     -- Себестоимость одной штуки
    margin NUMERIC(12, 2) GENERATED ALWAYS AS (quantity * unit_price - (cost_price * quantity)),      -- Маржа
    marketplace TEXT NOT NULL,                                                              -- Маркетплейс
    comment TEXT                                                                            -- Комментарий
);

CREATE TABLE IF NOT EXISTS purchases (
    id BIGSERIAL  PRIMARY KEY,                                              -- ID
    purchase_date TIMESTAMP DEFAULT NOW(),                                  -- Дата
    product_id INTEGER NOT NULL,                                            -- ID Товара
    product_name TEXT NOT NULL,                                             -- Название товара
    cost_price REAL NOT NULL,                                               -- Себестоимость
    quantity INTEGER NOT NULL CHECK (quantity > 0),                         -- Количество
    total_price NUMERIC(12, 2) GENERATED ALWAYS AS (cost_price * quantity), -- Общая сумма
    supplier TEXT NOT NULL,                                                 -- Поставщик
    comment TEXT                                                            -- Комментарий
);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL  PRIMARY KEY,              -- ID
    expense_date TIMESTAMP DEFAULT NOW(),   -- Дата
    category TEXT NOT NULL,                 -- Категория
    description TEXT NOT NULL,              -- Описание
    amount REAL NOT NULL,                   -- Сумма
    comment TEXT                            -- Комментарий
);

CREATE TABLE IF NOT EXISTS last_export (
    id BIGSERIAL PRIMARY KEY,
    export_date TIMESTAMP
)
