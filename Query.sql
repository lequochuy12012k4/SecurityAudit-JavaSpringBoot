INSERT INTO invoices (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    customer_email,
    customer_name,
    customer_phone,
    description,
    due_date,
    invoice_code,
    invoice_date,
    status,
    total_amount,
    version
)
SELECT 
    gen_random_uuid()::varchar(255) AS id,
    NOW() - (random() * interval '180 days') AS created_at,
    'SYSTEM' AS created_by,
    NOW() AS updated_at,
    'SYSTEM' AS updated_by,
    'customer_' || i || '@example.com' AS customer_email,
    'Khách hàng ' || i AS customer_name,
    '09' || lpad((floor(random() * 100000000))::text, 8, '0') AS customer_phone,
    'Hóa đơn thanh toán dịch vụ #' || i AS description,
    NOW() + (random() * interval '30 days') AS due_date,
    'INV-' || lpad(i::text, 8, '0') AS invoice_code, -- Mã hóa đơn: INV-00000001
    NOW() - (random() * interval '180 days') AS invoice_date,
    (ARRAY['PENDING', 'PAID', 'CANCELLED', 'OVERDUE'])[floor(random() * 4 + 1)] AS status,
    round((random() * 5000000 + 50000)::numeric, 2) AS total_amount, -- Số tiền ngẫu nhiên từ 50k đến 5tr
    1 AS version
FROM generate_series(1, 100000) AS i;

INSERT INTO users (
    id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    account_non_locked,
    email,
    enabled,
    full_name,
    password,
    username
)
SELECT 
    gen_random_uuid()::varchar(255) AS id,
    NOW() - (random() * interval '365 days') AS created_at,
    'SYSTEM' AS created_by,
    NOW() AS updated_at,
    'SYSTEM' AS updated_by,
    (random() > 0.1) AS account_non_locked,
    'user_' || i || '@example.com' AS email,
    (random() > 0.05) AS enabled,
    'User Name ' || i AS full_name,
    md5('user_' || i) AS password, -- Hash chuỗi username
    'user_' || i AS username
FROM generate_series(1, 100000) AS i;