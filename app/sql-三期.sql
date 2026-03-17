


ALTER TABLE `t_sku` ADD COLUMN `sku_status` varchar(64) NOT NULL default 'DISABLE' AFTER `sku_type`;

update t_sku set sku_status = 'ENABLE';

ALTER TABLE `t_sku` ADD COLUMN `description` text NULL AFTER `stock_ticket`;

ALTER TABLE `t_match` ADD COLUMN `end_sale_time` datetime NULL AFTER `start_sale_time`;

ALTER TABLE `t_match` ADD COLUMN `agreement_info` text NULL AFTER `buy_limit`;

ALTER TABLE `t_match` ADD COLUMN `ticket_show_info` text NULL AFTER `agreement_info`;

ALTER TABLE `t_match` ADD COLUMN `allow_refund` varchar(16) NULL AFTER `ticket_show_info`;

ALTER TABLE `t_match` ADD COLUMN `need_id_for_ticket` varchar(16) NOT NULL DEFAULT 'Y' COMMENT '购票是否需要身份证' AFTER `allow_refund`;

ALTER TABLE `t_match` ADD COLUMN `refund_rule` text NULL AFTER `allow_refund`;


ALTER TABLE `t_order` ADD COLUMN `name` varchar(32) NULL AFTER `user_id`;
ALTER TABLE `t_order` ADD COLUMN `sku_name` varchar(32) NULL AFTER `sku_id`;

ALTER TABLE `t_sku` ADD COLUMN `remark` varchar(256) NULL AFTER `stock_ticket`;

ALTER TABLE `t_ticket` ADD COLUMN `verification_status` varchar(32) NOT NULL DEFAULT 'N' AFTER `sync_status`;


ALTER TABLE `t_account` ADD COLUMN `role` varchar(32) NULL;
ALTER TABLE `t_account` ADD COLUMN `tenant_id` int NULL;
ALTER TABLE `t_account` ADD COLUMN `remark` text NULL;

ALTER TABLE `t_sku` ADD COLUMN `sort_number` int NOT NULL default 0 AFTER `stock_ticket`;


ALTER TABLE `t_match` ADD COLUMN `gray_match` varchar(32) NOT NULL DEFAULT 'N';


drop  table t_system_config;
create table t_system_config (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    `name` varchar(128) not null,
    `config_key` varchar(128) not null,
    `config_value` varchar(1024) not null,
    unique key uk_config_key (`config_key`)
) comment '系统配置表';

insert into t_system_config(`name`, `config_key`, `config_value`) values('灰度用户', 'gray_user_ids', ',5,111,');


-- Banner 运营表，仅首页 Banner
create table t_banner (
    id bigint unsigned primary key auto_increment,
    gmt_create timestamp default CURRENT_TIMESTAMP,
    gmt_modify timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    title varchar(128) null comment '标题',
    image_url varchar(512) not null comment '图片地址',
    jump_type varchar(32) null comment '跳转类型：NONE/H5/MATCH/SKU/EXTERNAL',
    jump_target varchar(512) null comment '跳转目标，如URL或matchId/skuId',
    sort_number int not null default 0 comment '排序，数字越小越靠前',
    status varchar(16) not null default 'Y' comment '是否启用，Y/N',
    start_time datetime null comment '开始生效时间',
    end_time datetime null comment '结束生效时间',
    remark text null comment '备注',
    deleted varchar(16) not null default 'N' comment '是否删除，Y/N'
) comment '首页 Banner 表';

