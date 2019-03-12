CREATE TABLE `saga_context` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Saga ID',
  `app_name` varchar(200) NOT NULL DEFAULT '' COMMENT '应用/系统名',
  `biz_name` varchar(200) NOT NULL DEFAULT '' COMMENT '业务名',
  `biz_id` varchar(200) NOT NULL DEFAULT '' COMMENT '业务ID',
  `status` varchar(10) NOT NULL DEFAULT '' COMMENT 'Saga 状态',
  `current_tx` varchar(200) DEFAULT '' COMMENT '当前正在执行的TX类',
  `pre_executed_tx` varchar(200) DEFAULT '' COMMENT '上一个执行的TX类',
  `pre_compensated_tx` varchar(20) DEFAULT '' COMMENT '上一个补偿的TX类',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_biz_id_uk` (`app_name`,`biz_name`,`biz_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `saga_context_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `context_id` bigint(20) NOT NULL,
  `key` varchar(200) NOT NULL DEFAULT '',
  `info` longblob NOT NULL,
  `create_time` datetime NOT NULL,
  `modify_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `context_key_uk` (`context_id`,`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `saga_context_lock` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `context_id` bigint(20) NOT NULL,
  `req_id` varchar(100) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL,
  `expire_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `context_id` (`context_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;