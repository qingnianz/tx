/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : localhost:3306
 Source Schema         : impl

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : 65001

 Date: 07/04/2020 22:44:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for transaction_local_message_table
-- ----------------------------
DROP TABLE IF EXISTS `transaction_local_message_table`;
CREATE TABLE `transaction_local_message_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `destination` varchar(255) NOT NULL COMMENT '目的地 topic',
  `secondary_destination` varchar(255) DEFAULT NULL COMMENT '二级目的地 tag',
  `message_key` varchar(255) DEFAULT NULL COMMENT '消息key',
  `message_content` blob NOT NULL COMMENT '消息内容',
  `message_status` tinyint(1) NOT NULL COMMENT '消息状态 0未投递或投递失败 1已投递',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `died` tinyint(1) NOT NULL DEFAULT '0' COMMENT '死信 0否 1是',
  `retry_count` int(2) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `description` varchar(3000) DEFAULT NULL COMMENT '备注',
  `service_name` varchar(255) NOT NULL COMMENT '服务名称',
  PRIMARY KEY (`id`),
  KEY `Idx_query` (`message_status`,`died`,`create_time`) USING BTREE COMMENT '用于待投递消息检索'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事务本地消息表';

SET FOREIGN_KEY_CHECKS = 1;
