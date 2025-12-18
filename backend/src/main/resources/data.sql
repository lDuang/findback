INSERT INTO users (id, username, password, role) VALUES
    (1, 'admin', 'admin', 'ADMIN'),
    (2, 'jane.doe', 'password123', 'USER');

INSERT INTO lost_items (id, user_id, title, description, status, location, image_url, created_at) VALUES
    (1, 2, 'Blue Backpack', 'Lost near the library entrance with notebooks inside.', 'OPEN', 'Library entrance', NULL, CURRENT_TIMESTAMP),
    (2, 2, 'Wireless Earbuds', 'Left in the cafeteria on a white table.', 'OPEN', 'Cafeteria', NULL, CURRENT_TIMESTAMP),
    (3, 2, 'Student ID Card', 'Last seen in the parking lot.', 'CLAIMED', 'Parking lot', NULL, CURRENT_TIMESTAMP);

INSERT INTO claims (id, item_id, user_id, status, reason, evidence_url, created_at) VALUES
    (1, 3, 2, 'APPROVED', 'Verified ownership with student ID photo.', NULL, CURRENT_TIMESTAMP);

INSERT INTO announcements (id, title, content, created_at) VALUES
    (1, 'Welcome', 'Welcome to the lost and found portal.', CURRENT_TIMESTAMP),
    (2, 'Reminder', 'Please label your belongings to help us return items faster.', CURRENT_TIMESTAMP);

INSERT INTO banners (id, title, image_url, link_url) VALUES
    (1, 'Campus Safety', 'https://example.com/banner.jpg', 'https://example.com/safety'),
    (2, 'Report Lost Items', 'https://example.com/lost-items.jpg', 'https://example.com/report');
