INSERT INTO task VALUES('00000000-0000-0000-0000-000000000001','title 001', 'description 001', NULL, FALSE, NULL, CURRENT_TIMESTAMP + '0 SECONDS', 0);
INSERT INTO task VALUES('00000000-0000-0000-0000-000000000002','title 002', 'description 002', NULL, FALSE, NULL, CURRENT_TIMESTAMP + '1 SECONDS', 0);
INSERT INTO task VALUES('00000000-1000-0000-0000-000000000003','title 003', 'description 003', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + '2 SECONDS', 0);
INSERT INTO task VALUES('00000000-1000-0000-0000-000000000004','title 004', 'description 004', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + '3 SECONDS', 0);

COMMIT;