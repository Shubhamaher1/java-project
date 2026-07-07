-- ============================================================
-- SEED DATA FOR H2 IN-MEMORY DATABASE
-- ============================================================
-- This file is auto-executed by Spring Boot on startup
-- when spring.jpa.hibernate.ddl-auto=create-drop or create
-- spring.sql.init.mode=always (default for embedded DBs)
--
-- Interview Q: What is the order of execution?
-- 1. schema.sql (DDL - table creation) - but since we use Hibernate ddl-auto, Hibernate creates tables
-- 2. data.sql  (DML - inserts)
-- Tip: set spring.jpa.defer-datasource-initialization=true
--      so Hibernate creates schema BEFORE data.sql runs
-- ============================================================

-- Departments
INSERT INTO departments (id, name, location, version) VALUES (1, 'Engineering',  'Pune',    0);
INSERT INTO departments (id, name, location, version) VALUES (2, 'HR',           'Mumbai',  0);
INSERT INTO departments (id, name, location, version) VALUES (3, 'Finance',      'Bangalore',0);
INSERT INTO departments (id, name, location, version) VALUES (4, 'Marketing',    'Delhi',   0);

-- Employees
INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (1, 'Rahul',   'Sharma',  'rahul.sharma@company.com',  85000.00, 'Engineering', 28, '9876543210', 'ACTIVE',   1);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (2, 'Priya',   'Patel',   'priya.patel@company.com',   92000.00, 'Engineering', 32, '9876543211', 'ACTIVE',   1);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (3, 'Amit',    'Kumar',   'amit.kumar@company.com',    75000.00, 'HR',          26, '9876543212', 'ACTIVE',   2);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (4, 'Sneha',   'Joshi',   'sneha.joshi@company.com',   68000.00, 'Finance',     30, '9876543213', 'ACTIVE',   3);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (5, 'Vikram',  'Singh',   'vikram.singh@company.com',  110000.00,'Engineering', 35, '9876543214', 'ACTIVE',   1);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (6, 'Ananya',  'Reddy',   'ananya.reddy@company.com',  78000.00, 'Marketing',   27, '9876543215', 'ACTIVE',   4);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (7, 'Rajesh',  'Gupta',   'rajesh.gupta@company.com',  55000.00, 'HR',          24, '9876543216', 'INACTIVE', 2);

INSERT INTO employees (id, first_name, last_name, email, salary, department, age, phone, status, dept_id)
VALUES (8, 'Meena',   'Iyer',    'meena.iyer@company.com',    95000.00, 'Engineering', 33, '9876543217', 'ACTIVE',   1);

-- Skills
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (1, 'Java',       'Expert',        1);
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (2, 'Spring Boot','Advanced',      1);
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (3, 'Kafka',      'Intermediate',  1);
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (4, 'Python',     'Advanced',      2);
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (5, 'AWS',        'Expert',        5);
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (6, 'Kubernetes', 'Intermediate',  5);
INSERT INTO skills (id, skill_name, proficiency, employee_id) VALUES (7, 'React',      'Advanced',      6);
