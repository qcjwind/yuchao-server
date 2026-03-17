-- ============================================================
-- 将生产库 t_system_config 数据插入测试库
-- ============================================================

-- ---------- 方式一：生产库与测试库在同一 MySQL 实例时 ----------
-- 在测试库执行（先清空测试表再插入，避免主键冲突）：
/*
TRUNCATE TABLE t_system_config;

INSERT INTO yuchao_test.t_system_config (id, gmt_create, gmt_modify, name, config_key, config_value)
SELECT id, gmt_create, gmt_modify, name, config_key, config_value
FROM yuchao.t_system_config;
*/

-- ---------- 方式二：生产、测试在不同实例（推荐） ----------
-- 步骤 1：在生产库（yuchao）执行下面的查询，将结果复制出来；
-- 步骤 2：在测试库执行复制出的 INSERT 语句。

-- 在生产库执行（生成可复制的 INSERT 语句）：
/*
SELECT CONCAT(
  'INSERT INTO t_system_config (id, gmt_create, gmt_modify, name, config_key, config_value) VALUES (',
  id, ', ',
  IFNULL(CONCAT('''', DATE_FORMAT(gmt_create, '%Y-%m-%d %H:%i:%s'), ''''), 'NULL'), ', ',
  IFNULL(CONCAT('''', DATE_FORMAT(gmt_modify, '%Y-%m-%d %H:%i:%s'), ''''), 'NULL'), ', ',
  QUOTE(name), ', ',
  QUOTE(config_key), ', ',
  QUOTE(config_value),
  ');'
) AS insert_sql
FROM t_system_config;
*/

-- ---------- 方式三：命令行导出再导入（生产/测试不同实例） ----------
-- 在生产服务器导出：
--   mysqldump -h 生产库IP -u 用户 -p yuchao t_system_config --no-create-info > t_system_config_data.sql
-- 在测试库导入前，可先清空： TRUNCATE TABLE t_system_config;
-- 在测试服务器导入：
--   mysql -h 测试库IP -u 用户 -p yuchao_test < t_system_config_data.sql


-- ========== 以下在【生产库】执行，将结果复制到测试库执行 ==========
-- 生成每条 INSERT 语句（在生产库执行后，复制 insert_sql 列的全部结果，到测试库执行）：
SELECT CONCAT(
  'INSERT INTO t_system_config (id, gmt_create, gmt_modify, name, config_key, config_value) VALUES (',
  id, ', ',
  IFNULL(CONCAT('''', DATE_FORMAT(gmt_create, '%Y-%m-%d %H:%i:%s'), ''''), 'NULL'), ', ',
  IFNULL(CONCAT('''', DATE_FORMAT(gmt_modify, '%Y-%m-%d %H:%i:%s'), ''''), 'NULL'), ', ',
  QUOTE(name), ', ',
  QUOTE(config_key), ', ',
  QUOTE(config_value),
  ');'
) AS insert_sql
FROM t_system_config;
