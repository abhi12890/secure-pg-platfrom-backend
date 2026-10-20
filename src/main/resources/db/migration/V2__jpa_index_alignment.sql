CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_rooms_number ON rooms (room_number);
CREATE INDEX IF NOT EXISTS idx_tenant_user_id ON tenant_profiles (user_id);
