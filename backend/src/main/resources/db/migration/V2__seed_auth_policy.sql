INSERT INTO auth_policy_version(version, status, checksum, created_by, approved_by, approved_at)
VALUES (1, 'ACTIVE', 'seed-v1', 'system', 'system', NOW());

INSERT INTO auth_policy_item(policy_version_id, policy_key, value_json, description)
SELECT id, 'OTP_LENGTH', '6'::jsonb, 'OTP digit count' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'OTP_TTL_SECONDS', '120'::jsonb, 'OTP lifetime' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'OTP_MAX_ATTEMPTS', '5'::jsonb, 'Maximum wrong OTP attempts' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION_FLOW_TTL_SECONDS', '1800'::jsonb, 'Registration flow lifetime' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'LOGIN_CHALLENGE_TTL_SECONDS', '300'::jsonb, 'New device challenge lifetime' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'LOGIN_MAX_ATTEMPTS', '5'::jsonb, 'Failed login limit' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'LOGIN_BLOCK_SECONDS', '900'::jsonb, 'Failed login window / temporary block' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'ACCESS_TOKEN_TTL_SECONDS', '600'::jsonb, 'JWT access token lifetime' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REFRESH_TOKEN_TTL_SECONDS', '2592000'::jsonb, 'Refresh token lifetime' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'TRUSTED_DEVICE_DAYS', '30'::jsonb, 'Trusted device validity' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'PASSWORD_MIN_LENGTH', '10'::jsonb, 'Password minimum length' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'PASSWORD_REQUIRE_DIGIT', 'true'::jsonb, 'Password requires digit' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'PASSWORD_REQUIRE_UPPER', 'true'::jsonb, 'Password requires uppercase' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'PASSWORD_REQUIRE_LOWER', 'true'::jsonb, 'Password requires lowercase' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'PASSWORD_REQUIRE_SPECIAL', 'true'::jsonb, 'Password requires special char' FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'PIN_LENGTH', '4'::jsonb, 'Application PIN length' FROM auth_policy_version WHERE version=1;

INSERT INTO auth_ui_config(policy_version_id, flow_code, step_code, order_no, min_app_version, config_json)
SELECT id, 'REGISTRATION', 'PHONE', 1, '1.0.0',
'{
  "title":{"uz":"Telefon raqam","ru":"Номер телефона","en":"Phone number"},
  "fields":[
    {"name":"phone","type":"phone","label":{"uz":"Telefon","ru":"Телефон","en":"Phone"},"required":true,"prefix":"+998"}
  ]
}'::jsonb
FROM auth_policy_version WHERE version=1
UNION ALL
SELECT id, 'REGISTRATION', 'OTP', 2, '1.0.0',
'{
  "title":{"uz":"Tasdiqlash","ru":"Подтверждение","en":"Verification"},
  "fields":[
    {"name":"otp","type":"otp","label":{"uz":"SMS kod","ru":"SMS код","en":"SMS code"},"required":true,"length":6}
  ]
}'::jsonb
FROM auth_policy_version WHERE version=1
UNION ALL
SELECT id, 'REGISTRATION', 'IDENTITY', 3, '1.0.0',
'{
  "title":{"uz":"Shaxsiy ma''lumotlar","ru":"Личные данные","en":"Identity"},
  "fields":[
    {"name":"pinfl","type":"pinfl","label":{"uz":"JSHSHIR","ru":"ПИНФЛ","en":"PINFL"},"required":true,"maxLength":14},
    {"name":"passport","type":"passport","label":{"uz":"Pasport","ru":"Паспорт","en":"Passport"},"required":true,"mask":"AA #######"}
  ]
}'::jsonb
FROM auth_policy_version WHERE version=1
UNION ALL
SELECT id, 'REGISTRATION', 'PASSWORD', 4, '1.0.0',
'{
  "title":{"uz":"Parol yaratish","ru":"Создание пароля","en":"Create password"},
  "fields":[
    {"name":"password","type":"password","label":{"uz":"Parol","ru":"Пароль","en":"Password"},"required":true}
  ]
}'::jsonb
FROM auth_policy_version WHERE version=1
UNION ALL
SELECT id, 'REGISTRATION', 'PIN', 5, '1.0.0',
'{
  "title":{"uz":"PIN kod","ru":"ПИН код","en":"PIN"},
  "fields":[
    {"name":"pin","type":"pin","label":{"uz":"PIN kod","ru":"ПИН код","en":"PIN"},"required":true,"length":4}
  ]
}'::jsonb
FROM auth_policy_version WHERE version=1
UNION ALL
SELECT id, 'LOGIN', 'LOGIN', 1, '1.0.0',
'{
  "title":{"uz":"Kirish","ru":"Вход","en":"Login"},
  "fields":[
    {"name":"phone","type":"phone","label":{"uz":"Telefon","ru":"Телефон","en":"Phone"},"required":true,"prefix":"+998"},
    {"name":"password","type":"password","label":{"uz":"Parol","ru":"Пароль","en":"Password"},"required":true}
  ]
}'::jsonb
FROM auth_policy_version WHERE version=1;

INSERT INTO auth_validation_map(policy_version_id, domain_code, step_code, field_code, validator_code, order_no)
SELECT id, 'REGISTRATION', 'PHONE', 'phone', 'PHONE_FORMAT', 1 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'PHONE', 'phone', 'PHONE_NOT_REGISTERED', 2 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'IDENTITY', 'pinfl', 'PINFL_FORMAT', 1 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'IDENTITY', 'pinfl', 'PINFL_NOT_REGISTERED', 2 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'IDENTITY', 'passport', 'PASSPORT_FORMAT', 3 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'IDENTITY', 'passport', 'PASSPORT_NOT_REGISTERED', 4 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'PASSWORD', 'password', 'PASSWORD_POLICY', 1 FROM auth_policy_version WHERE version=1
UNION ALL SELECT id, 'REGISTRATION', 'PIN', 'pin', 'PIN_FORMAT', 1 FROM auth_policy_version WHERE version=1;
