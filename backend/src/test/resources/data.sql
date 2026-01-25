-- Test seed data

MERGE INTO sys_user (id, username, password, nickname, email, phone, status, deleted) KEY(id) VALUES (
  1,
  'admin',
  '$2a$10$XENv3eA.D2gjGiPCgBhliut7eOuM2lCH87x4L0SjwCdpa5oAlMRo2',
  'Admin',
  'admin@example.com',
  '13800000000',
  1,
  0
);

MERGE INTO sys_role (id, code, name, description, deleted) KEY(id)
VALUES (10, 'admin', '管理员', '系统管理员', 0);

MERGE INTO sys_role (id, code, name, description, deleted) KEY(id)
VALUES (11, 'user', '普通用户', '普通用户', 0);

MERGE INTO sys_user_role (id, user_id, role_id) KEY(id)
VALUES (1000, 1, 10);

MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (100, 'user:list', '用户列表', '查看用户列表', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (101, 'user:create', '创建用户', '创建用户', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (102, 'user:update', '更新用户', '更新用户', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (103, 'user:delete', '删除用户', '删除用户', 0);

MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (110, 'role:list', '角色列表', '查看角色列表', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (111, 'role:create', '创建角色', '创建角色', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (112, 'role:update', '更新角色', '更新角色', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (113, 'role:delete', '删除角色', '删除角色', 0);

MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (120, 'permission:list', '权限列表', '查看权限列表', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (121, 'permission:create', '创建权限', '创建权限', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (122, 'permission:update', '更新权限', '更新权限', 0);
MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (123, 'permission:delete', '删除权限', '删除权限', 0);

MERGE INTO sys_permission (id, code, name, description, deleted) KEY(id) VALUES (130, 'notification:announce', '系统公告', '发布系统公告', 0);

MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2000, 10, 100);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2001, 10, 101);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2002, 10, 102);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2003, 10, 103);

MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2010, 10, 110);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2011, 10, 111);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2012, 10, 112);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2013, 10, 113);

MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2020, 10, 120);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2021, 10, 121);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2022, 10, 122);
MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2023, 10, 123);

MERGE INTO sys_role_permission (id, role_id, permission_id) KEY(id) VALUES (2030, 10, 130);
