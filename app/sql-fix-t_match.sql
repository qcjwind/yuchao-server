-- 若 t_match 报错 Unknown column 'need_id_for_ticket' 或 'gray_match'，在测试库执行以下语句（已存在的列会报错，可跳过）：

-- 购票是否需要身份证（缺则执行）
ALTER TABLE `t_match` ADD COLUMN `need_id_for_ticket` varchar(16) NOT NULL DEFAULT 'Y' COMMENT '购票是否需要身份证' AFTER `allow_refund`;

-- 灰度赛事标识（缺则执行）
ALTER TABLE `t_match` ADD COLUMN `gray_match` varchar(32) NOT NULL DEFAULT 'N' AFTER `refund_rule`;
