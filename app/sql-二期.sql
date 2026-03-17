

use yuchao;

alter table t_ticket add unique key uk_sku_id_sub_area_seat (`sku_id`, `sub_area`, `seat_row`, `seat_no`);
alter table t_ticket add unique key uk_match_id_sub_area_seat (`match_id`, `sub_area`, `seat_row`, `seat_no`);


ALTER TABLE `t_ticket` MODIFY COLUMN `price` int NULL DEFAULT 0;
ALTER TABLE `t_order` MODIFY COLUMN `total_price` int NULL DEFAULT 0;
ALTER TABLE `t_sku` MODIFY COLUMN `price` int NULL DEFAULT 0;


ALTER TABLE `t_order` ADD COLUMN `wx_prepay_id` varchar(64) NULL AFTER `order_no`;
ALTER TABLE `t_order` ADD COLUMN `wx_refund_id` varchar(64) NULL AFTER `wx_prepay_id`;


ALTER TABLE `t_order` ADD COLUMN `refund_price` int NULL AFTER `total_price`;
ALTER TABLE `t_order` ADD COLUMN `order_status` varchar(64) NULL AFTER `refund_price`;
ALTER TABLE `t_order` ADD COLUMN `order_time` datetime NULL AFTER `order_status`;
ALTER TABLE `t_order` ADD COLUMN `pay_time` datetime NULL AFTER `order_time`;
ALTER TABLE `t_order` ADD COLUMN `refund_time` datetime NULL AFTER `pay_time`;
ALTER TABLE `t_order` ADD COLUMN `order_info` text NULL;
ALTER TABLE `t_order` ADD COLUMN `pay_info` text NULL;

update t_order set order_status = 'PAY_SUCCESS', order_time = gmt_create, pay_time = gmt_create;


ALTER TABLE `t_ticket` MODIFY COLUMN `order_id` bigint NULL;

ALTER TABLE `t_match` ADD COLUMN `match_tags` varchar(128) NULL AFTER `matinee_name`;

INSERT INTO `t_match`(`id`, `gmt_create`, `gmt_modify`, `name`, `detail`, `cover`, `matinee_name`, `match_tags`, `status`, `sale_status`, `venue_id`, `buy_limit`, `gift_ticket_url`, `gate_url`, `gate_token`, `start_sale_time`, `start_time`, `end_time`) VALUES (12, '2025-09-30 14:49:24', '2025-10-09 17:28:45', '“新韵重庆 渝超同行”酆都', '<div style=\"text-align:left\"><img src=\"https://mdn.alipayobjects.com/huamei_1iaqio/afts/img/A*TQ73SJtQQ20AAAAAgBAAAAgAelx8AQ/original\" alt=\"图片 alt\" width=\"100%\" height=\"auto\" data-align=\"left\"></div><p></p>', 'https://cy25yuchao.oss-cn-chengdu.aliyuncs.com/images/8c34e510-fd3d-4092-9fb5-7fd09f90346a.png', NULL, 'DISABLE_SEAT', 'ENABLE', 'NOT_FINISH', 1, 2, 'https://cy25yuchao.oss-cn-chengdu.aliyuncs.com/upload/20250930_144924-ticket.zip', 'http://yc1.zszlchina.com/es-server/api/push/person', 'b095bb12d6b844c995be85473cd45cc5', '2025-10-01 10:00:00', '2025-10-08 15:00:00', '2025-10-28 18:00:00');

