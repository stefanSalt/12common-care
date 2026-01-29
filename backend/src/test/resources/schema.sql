-- H2 schema for tests (MODE=MySQL)

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  phone VARCHAR(32) DEFAULT NULL,
  avatar_file_id BIGINT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user(status);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role(code);

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission(code);

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_role ON sys_user_role(user_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role(role_id);

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_permission ON sys_role_permission(role_id, permission_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_permission_permission_id ON sys_role_permission(permission_id);

CREATE TABLE IF NOT EXISTS sys_file (
  id BIGINT NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  path VARCHAR(512) NOT NULL,
  size BIGINT NOT NULL,
  content_type VARCHAR(128) DEFAULT NULL,
  visibility VARCHAR(16) NOT NULL DEFAULT 'PRIVATE',
  user_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_file_path ON sys_file(path);
CREATE INDEX IF NOT EXISTS idx_sys_file_user_id ON sys_file(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_file_visibility ON sys_file(visibility);

CREATE TABLE IF NOT EXISTS sys_notification (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  type VARCHAR(32) NOT NULL,
  is_read TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_sys_notification_user_created ON sys_notification(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_sys_notification_user_read ON sys_notification(user_id, is_read);

CREATE TABLE IF NOT EXISTS sys_message (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  contact_email VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  reply_content TEXT DEFAULT NULL,
  replied_at TIMESTAMP DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_sys_message_user_created ON sys_message(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_sys_message_status ON sys_message(status);

CREATE TABLE IF NOT EXISTS biz_banner (
  id BIGINT NOT NULL,
  title VARCHAR(128) DEFAULT NULL,
  image_file_id BIGINT NOT NULL,
  link_url VARCHAR(512) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_biz_banner_enabled_sort ON biz_banner(enabled, sort_no);
CREATE INDEX IF NOT EXISTS idx_biz_banner_image_file_id ON biz_banner(image_file_id);

CREATE TABLE IF NOT EXISTS biz_activity (
  id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  cover_file_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  address VARCHAR(255) DEFAULT NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  signup_enabled TINYINT NOT NULL DEFAULT 1,
  donate_enabled TINYINT NOT NULL DEFAULT 1,
  max_participants INT DEFAULT NULL,
  donation_target DECIMAL(12,2) DEFAULT NULL,
  donated_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_biz_activity_enabled_time ON biz_activity(enabled, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_biz_activity_cover_file_id ON biz_activity(cover_file_id);

CREATE TABLE IF NOT EXISTS biz_activity_signup (
  id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL,
  signed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  canceled_at TIMESTAMP DEFAULT NULL,
  checked_in_at TIMESTAMP DEFAULT NULL,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_biz_activity_signup ON biz_activity_signup(activity_id, user_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_signup_activity_id ON biz_activity_signup(activity_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_signup_user_id ON biz_activity_signup(user_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_signup_signed_at ON biz_activity_signup(signed_at);

CREATE TABLE IF NOT EXISTS biz_activity_donation (
  id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_biz_activity_donation_activity_id ON biz_activity_donation(activity_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_donation_user_id ON biz_activity_donation(user_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_donation_created_at ON biz_activity_donation(created_at);

CREATE TABLE IF NOT EXISTS biz_activity_favorite (
  id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_biz_activity_favorite ON biz_activity_favorite(activity_id, user_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_favorite_activity_id ON biz_activity_favorite(activity_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_favorite_user_id ON biz_activity_favorite(user_id);
CREATE INDEX IF NOT EXISTS idx_biz_activity_favorite_created_at ON biz_activity_favorite(created_at);
