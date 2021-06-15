use duskpay;

INSERT INTO status(id, name, created_by, created_date, updated_by, last_update) VALUES
    (0, 'USER_SUSPENDED_BY_SUPERADMIN', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (1, 'USER_SELF_SUSPENDED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (2, 'USER_ACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (3, 'USER_DISABLED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),

    (4, 'ENTERPRISE_NULL', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (5, 'ENTERPRISE_NOT_VERIFIED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (6, 'ENTERPRISE_BEING_VERIFIED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (7, 'ENTERPRISE_VERIFIED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    
    (8, 'COLLECTOR_ACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (9, 'COLLECTOR_SUSPENDED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (10, 'COLLECTOR_DEACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    
    (11, 'TRANSACTION_CONFIRMED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (12, 'TRANSACTION_IN_CONFIRMATION', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (13, 'TRANSACTION_INITIATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (14, 'TRANSACTION_CRYPTO_New', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (15, 'TRANSACTION_CRYPTO_Expired', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (16, 'TRANSACTION_CRYPTO_Paid', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (17, 'TRANSACTION_CRYPTO_Confirmed', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (18, 'TRANSACTION_CRYPTO_Complete', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (19, 'TRANSACTION_CRYPTO_Invalid', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),

    (20, 'API_ACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (21, 'API_DEACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    
    (22, 'API_COLLECTOR_ACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW()),
    (23, 'API_COLLECTOR_DEACTIVATED', 'APPLICATION_DEFAULT_USER', NOW(), 'APPLICATION_DEFAULT_USER', NOW());
