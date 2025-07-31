INSERT INTO payments (id, status, payment_type, rental_id, session_url, session_id, amount_to_pay, is_deleted)
VALUES
    (1, 'PAID', 'PAYMENT', 1, 'http://example.com/success1', 'sess_1', 400.00, 0),
    (2, 'PENDING', 'PAYMENT', 2, 'http://example.com/success2', 'sess_2', 200.00, 0),
    (3, 'PAID', 'FINE', 2, 'http://example.com/success3', 'sess_3', 50.00, 0);
