-- 若 t_system_config 报错 Unknown column 'config_key'，说明表结构缺少这两列，执行以下语句：
ALTER TABLE `t_system_config` ADD COLUMN `config_key` varchar(128) NOT NULL DEFAULT '' COMMENT '配置键' AFTER `name`;
ALTER TABLE `t_system_config` ADD COLUMN `config_value` varchar(1024) NOT NULL DEFAULT '' COMMENT '配置值' AFTER `config_key`;
