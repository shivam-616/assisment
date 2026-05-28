CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('TEACHER', 'PARENT')),
                       timezone VARCHAR(50) -- e.g., 'Asia/Kolkata'
);

CREATE TABLE courses (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         title VARCHAR(255) NOT NULL,
                         description TEXT
);

CREATE TABLE offerings (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           course_id UUID NOT NULL REFERENCES courses(id),
                           teacher_id UUID NOT NULL REFERENCES users(id),
                           title VARCHAR(255) NOT NULL,
                           status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED'))
);

CREATE TABLE sessions (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          offering_id UUID NOT NULL REFERENCES offerings(id) ON DELETE CASCADE,
                          start_time_utc TIMESTAMP NOT NULL, -- Pure UTC Epoch
                          end_time_utc TIMESTAMP NOT NULL,   -- Pure UTC Epoch
                          teacher_timezone VARCHAR(50) NOT NULL
);

CREATE TABLE bookings (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          offering_id UUID NOT NULL REFERENCES offerings(id),
                          parent_id UUID NOT NULL REFERENCES users(id),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          status VARCHAR(20) NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED')),
                          UNIQUE(offering_id, parent_id) -- A parent cannot book the same offering twice
);

-- Indexes optimized for the Conflict Detection Query (Phase 4)
CREATE INDEX idx_sessions_offering_id ON sessions(offering_id);
CREATE INDEX idx_sessions_time_range ON sessions(start_time_utc, end_time_utc);
CREATE INDEX idx_bookings_parent_status ON bookings(parent_id, status);