-- Example complex SQLs
-- 1) Join and aggregates
SELECT u.username, COUNT(o.id) AS orders_count, SUM(o.total) AS total_spent
FROM users u LEFT JOIN orders o ON u.id=o.user_id
GROUP BY u.username
HAVING COUNT(o.id) >= 0;

-- 2) Subquery: products more expensive than avg
SELECT * FROM products p WHERE price > (SELECT AVG(price) FROM products);

-- 3) Index usage (note: in-memory H2, create index for demo)
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
