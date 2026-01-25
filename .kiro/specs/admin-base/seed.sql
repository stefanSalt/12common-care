-- Seed data for admin-base (MySQL 8.0)
-- Usage (example):
-- 1) Create database (optional): CREATE DATABASE admin_base DEFAULT CHARACTER SET utf8mb4;
-- 2) Import schema: source schema.sql
-- 3) Import seed:  source seed.sql

SET NAMES utf8mb4;

-- admin / admin123 (BCrypt hash)
INSERT INTO sys_user (id, username, password, nickname, email, phone, status, deleted)
VALUES (
  1,
  'admin',
  '$2a$10$XENv3eA.D2gjGiPCgBhliut7eOuM2lCH87x4L0SjwCdpa5oAlMRo2',
  'Admin',
  'admin@example.com',
  '13800000000',
  1,
  0
);

INSERT INTO sys_role (id, code, name, description, deleted)
VALUES (10, 'admin', '管理员', '系统管理员', 0);

INSERT INTO sys_user_role (id, user_id, role_id)
VALUES (1000, 1, 10);

INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (100, 'user:list', '用户列表', '查看用户列表', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (101, 'user:create', '创建用户', '创建用户', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (102, 'user:update', '更新用户', '更新用户', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (103, 'user:delete', '删除用户', '删除用户', 0);

INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (110, 'role:list', '角色列表', '查看角色列表', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (111, 'role:create', '创建角色', '创建角色', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (112, 'role:update', '更新角色', '更新角色', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (113, 'role:delete', '删除角色', '删除角色', 0);

INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (120, 'permission:list', '权限列表', '查看权限列表', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (121, 'permission:create', '创建权限', '创建权限', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (122, 'permission:update', '更新权限', '更新权限', 0);
INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (123, 'permission:delete', '删除权限', '删除权限', 0);

INSERT INTO sys_permission (id, code, name, description, deleted) VALUES (130, 'notification:announce', '系统公告', '发布系统公告', 0);

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2000, 10, 100);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2001, 10, 101);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2002, 10, 102);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2003, 10, 103);

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2010, 10, 110);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2011, 10, 111);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2012, 10, 112);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2013, 10, 113);

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2020, 10, 120);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2021, 10, 121);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2022, 10, 122);
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2023, 10, 123);

INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (2030, 10, 130);

