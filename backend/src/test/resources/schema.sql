-- H2 schema for tests (MODE=MySQL)

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  phone VARCHAR(32) DEFAULT NULL,
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
