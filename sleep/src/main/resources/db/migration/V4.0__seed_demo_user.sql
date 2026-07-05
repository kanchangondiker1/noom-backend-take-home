
-- Seed a deterministic demo user so the API can be exercised out of the box
-- (via the Postman collection / test script) without an onboarding endpoint.
-- Pass this id in the `X-User-Id` header.
INSERT INTO users (id, username)
VALUES ('11111111-1111-1111-1111-111111111111', 'demo-user')
ON CONFLICT (id) DO NOTHING;
