/*
 Navicat Premium Data Transfer

 Source Server         : mysql8.0.33阿里云
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : 8.148.253.180:3306
 Source Schema         : yixiu

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 13/01/2026 15:49:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base`  (
  `knowledge_id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识编号',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类（系统、硬件、软件等）',
  `problem_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题描述',
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解决方案',
  `source` enum('manual_entry','auto_extracted','user_log') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user_log' COMMENT '数据来源',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`knowledge_id`) USING BTREE,
  INDEX `idx_knowledge_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI义修助手知识库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_knowledge_base
-- ----------------------------

-- ----------------------------
-- Table structure for ai_query_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_query_log`;
CREATE TABLE `ai_query_log`  (
  `query_id` bigint NOT NULL AUTO_INCREMENT COMMENT '查询编号',
  `user_id` bigint NOT NULL COMMENT '提问用户',
  `query_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提问内容',
  `ai_response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI回复内容',
  `related_knowledge_id` bigint NULL DEFAULT NULL COMMENT '关联知识ID',
  `query_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间',
  PRIMARY KEY (`query_id`) USING BTREE,
  INDEX `fk_query_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_query_knowledge`(`related_knowledge_id` ASC) USING BTREE,
  CONSTRAINT `fk_query_knowledge` FOREIGN KEY (`related_knowledge_id`) REFERENCES `ai_knowledge_base` (`knowledge_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_query_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI问答记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_query_log
-- ----------------------------

-- ----------------------------
-- Table structure for broadcast_read
-- ----------------------------
DROP TABLE IF EXISTS `broadcast_read`;
CREATE TABLE `broadcast_read`  (
  `check_id` bigint NOT NULL AUTO_INCREMENT,
  `notify_id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`check_id`) USING BTREE,
  UNIQUE INDEX `uniq_notify_user`(`notify_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_notify` FOREIGN KEY (`notify_id`) REFERENCES `notification` (`notify_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 205 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知已读记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of broadcast_read
-- ----------------------------
INSERT INTO `broadcast_read` VALUES (1, 4, 10, 0, '2025-12-18 21:15:22', '2025-12-18 21:15:22');
INSERT INTO `broadcast_read` VALUES (2, 4, 11, 1, '2025-12-18 21:15:22', '2025-12-19 15:15:07');
INSERT INTO `broadcast_read` VALUES (3, 4, 12, 0, '2025-12-18 21:15:22', '2025-12-18 21:15:22');
INSERT INTO `broadcast_read` VALUES (4, 4, 17, 0, '2025-12-18 21:15:22', '2025-12-18 21:15:22');
INSERT INTO `broadcast_read` VALUES (5, 4, 16, 1, '2025-12-18 21:15:22', '2026-01-08 17:30:34');
INSERT INTO `broadcast_read` VALUES (6, 4, 13, 1, '2025-12-18 21:15:22', '2025-12-29 14:39:35');
INSERT INTO `broadcast_read` VALUES (19, 7, 10, 0, '2025-12-19 15:31:34', '2025-12-19 15:31:34');
INSERT INTO `broadcast_read` VALUES (20, 7, 11, 1, '2025-12-19 15:31:34', '2025-12-19 15:46:22');
INSERT INTO `broadcast_read` VALUES (21, 7, 12, 0, '2025-12-19 15:31:34', '2025-12-19 15:31:34');
INSERT INTO `broadcast_read` VALUES (22, 7, 17, 0, '2025-12-19 15:31:34', '2025-12-19 15:31:34');
INSERT INTO `broadcast_read` VALUES (23, 7, 16, 1, '2025-12-19 15:31:34', '2026-01-08 17:30:33');
INSERT INTO `broadcast_read` VALUES (24, 7, 13, 1, '2025-12-19 15:31:34', '2025-12-29 14:39:33');

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user_like`(`comment_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_like
-- ----------------------------

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈编号',
  `request_id` bigint NOT NULL COMMENT '报修单编号',
  `user_id` bigint NOT NULL COMMENT '评论人',
  `rating` int NULL DEFAULT 5 COMMENT '评分（1~5）',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字评论',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `fk_feedback_request`(`request_id` ASC) USING BTREE,
  INDEX `fk_feedback_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of feedback
-- ----------------------------

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notify_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT '接收者用户ID，NULL表示全体公告',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者用户ID（系统通知可为NULL）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `type` enum('USER','SYSTEM','BROADCAST') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notify_id`) USING BTREE,
  INDEX `idx_receiver_read`(`receiver_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 99 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (4, 0, 0, 'test', 'test_content', 'BROADCAST', 0, NULL, '2025-12-18 21:15:22');
INSERT INTO `notification` VALUES (7, 0, 0, '测试标题', '测试内容', 'BROADCAST', 0, '', '2025-12-19 15:31:34');
INSERT INTO `notification` VALUES (8, 11, 13, '用户单发消息', '用户单发消息内容测试', 'USER', 1, NULL, '2025-12-19 16:08:09');
INSERT INTO `notification` VALUES (9, 11, 13, '我是管理员', '你干嘛', 'USER', 1, NULL, '2025-12-19 16:15:23');
INSERT INTO `notification` VALUES (10, 16, 11, 'byd', '你是一个一个啊啊啊啊！', 'USER', 1, NULL, '2025-12-20 20:33:18');
INSERT INTO `notification` VALUES (14, 10, 0, '报修审核通过通知', '您的电脑维修申请(报修编号: 5)已通过审核，请继续等待义修志愿者接收任务', 'SYSTEM', 1, '/repair/history', '2025-12-21 16:28:24');
INSERT INTO `notification` VALUES (15, 17, 13, '大坝的', '你是一个一个啊啊啊啊啊', 'USER', 1, '', '2025-12-21 18:19:01');
INSERT INTO `notification` VALUES (16, 10, 13, '大坝的', '你是一个一个啊啊啊啊啊', 'USER', 1, '', '2025-12-21 18:20:12');
INSERT INTO `notification` VALUES (17, 11, 0, '报修审核通过通知', '您的电脑维修申请(申请编号: 10)已通过审核，请继续等待义修志愿者接收任务', 'SYSTEM', 0, '/repair/history', '2025-12-26 13:16:50');
INSERT INTO `notification` VALUES (18, 17, 0, '报修审核通过通知', '您的电脑维修申请(申请编号: 21)已通过审核，请继续等待义修志愿者接收任务', 'SYSTEM', 0, '/repair/history', '2025-12-26 13:22:14');
INSERT INTO `notification` VALUES (19, 17, 0, '报修审核通过通知', '您的电脑维修申请(申请编号: 20)已通过审核，请继续等待义修志愿者接收任务', 'SYSTEM', 0, '/repair/history', '2025-12-26 13:22:16');
INSERT INTO `notification` VALUES (20, 17, 0, '报修审核通过通知', '您的电脑维修申请(申请编号: 24)已通过审核，请继续等待义修志愿者接收任务', 'SYSTEM', 0, '/repair/history', '2025-12-26 14:00:12');
INSERT INTO `notification` VALUES (21, 19, 13, '大坝的', '你是一个一个啊啊啊啊！', 'USER', 1, NULL, '2025-12-28 20:56:54');
INSERT INTO `notification` VALUES (22, 18, 13, 'title', 'test content', 'USER', 1, NULL, '2025-12-29 14:06:42');
INSERT INTO `notification` VALUES (25, 11, 13, 'title', 'test content', 'USER', 0, NULL, '2025-12-29 22:28:09');
INSERT INTO `notification` VALUES (44, 16, 13, '账户身份变更通知', '您的账户身份已由【志愿者】更改为【管理员】', 'USER', 1, NULL, '2026-01-08 17:40:19');
INSERT INTO `notification` VALUES (45, 16, 13, '账户身份变更通知', '您的账户身份已由【管理员】更改为【志愿者】', 'USER', 1, NULL, '2026-01-08 17:43:34');
INSERT INTO `notification` VALUES (46, 16, 13, '账户身份变更通知', '您的账户身份已由【志愿者】更改为【管理员】', 'USER', 1, NULL, '2026-01-08 17:44:16');
INSERT INTO `notification` VALUES (97, 21, 13, '账户身份变更通知', '您的账户身份已由【志愿者】更改为【管理员】', 'USER', 1, NULL, '2026-01-09 16:27:12');
INSERT INTO `notification` VALUES (98, 18, 13, '账户身份变更通知', '您的账户身份已由【志愿者】更改为【管理员】', 'USER', 0, NULL, '2026-01-09 16:29:42');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子正文',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0正常 1隐藏 2删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`) USING BTREE,
  INDEX `idx_post_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post
-- ----------------------------

-- ----------------------------
-- Table structure for post_comment
-- ----------------------------
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0正常 1删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_comment_post`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子一级评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_comment
-- ----------------------------

-- ----------------------------
-- Table structure for post_comment_reply
-- ----------------------------
DROP TABLE IF EXISTS `post_comment_reply`;
CREATE TABLE `post_comment_reply`  (
  `reply_id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL COMMENT '所属一级评论',
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NULL DEFAULT NULL COMMENT '被回复用户',
  `parent_reply_id` bigint NULL DEFAULT NULL COMMENT '父回复ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0正常 1删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`reply_id`) USING BTREE,
  INDEX `idx_reply_comment`(`comment_id` ASC) USING BTREE,
  CONSTRAINT `fk_reply_comment` FOREIGN KEY (`comment_id`) REFERENCES `post_comment` (`comment_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子二级评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_comment_reply
-- ----------------------------

-- ----------------------------
-- Table structure for post_favorite
-- ----------------------------
DROP TABLE IF EXISTS `post_favorite`;
CREATE TABLE `post_favorite`  (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `uk_post_user_fav`(`post_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for post_img
-- ----------------------------
DROP TABLE IF EXISTS `post_img`;
CREATE TABLE `post_img`  (
  `img_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`img_id`) USING BTREE,
  INDEX `idx_post_img`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_post_img` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_img
-- ----------------------------

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_post_user_like`(`post_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_like
-- ----------------------------

-- ----------------------------
-- Table structure for post_tag
-- ----------------------------
DROP TABLE IF EXISTS `post_tag`;
CREATE TABLE `post_tag`  (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_tag
-- ----------------------------
INSERT INTO `post_tag` VALUES (1, '求助', 0, '2026-01-13 15:47:49');
INSERT INTO `post_tag` VALUES (2, '经验分享', 0, '2026-01-13 15:47:59');
INSERT INTO `post_tag` VALUES (3, '资源分享', 0, '2026-01-13 15:48:08');
INSERT INTO `post_tag` VALUES (4, '杂谈', 0, '2026-01-13 15:48:23');
INSERT INTO `post_tag` VALUES (5, '精华', 0, '2026-01-13 15:49:06');

-- ----------------------------
-- Table structure for post_tag_rel
-- ----------------------------
DROP TABLE IF EXISTS `post_tag_rel`;
CREATE TABLE `post_tag_rel`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_tag`(`post_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `fk_ptr_tag`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `fk_ptr_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ptr_tag` FOREIGN KEY (`tag_id`) REFERENCES `post_tag` (`tag_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_tag_rel
-- ----------------------------

-- ----------------------------
-- Table structure for post_view
-- ----------------------------
DROP TABLE IF EXISTS `post_view`;
CREATE TABLE `post_view`  (
  `view_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NULL DEFAULT NULL COMMENT '未登录用户为空',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`view_id`) USING BTREE,
  INDEX `idx_view_post`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子浏览记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_view
-- ----------------------------

-- ----------------------------
-- Table structure for repair_assignment
-- ----------------------------
DROP TABLE IF EXISTS `repair_assignment`;
CREATE TABLE `repair_assignment`  (
  `assign_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分配编号',
  `request_id` bigint NOT NULL COMMENT '报修单号',
  `volunteer_id` bigint NOT NULL COMMENT '维修志愿者编号',
  `is_leader` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为队长（1=是，0=否）',
  `assigned_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态：0：任务进行中及未完成日志填写，1：已完成日志填写，5：申请加入队伍中，6：被拒绝进队伍',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注说明',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`assign_id`) USING BTREE,
  INDEX `fk_assign_request`(`request_id` ASC) USING BTREE,
  INDEX `fk_assign_volunteer`(`volunteer_id` ASC) USING BTREE,
  INDEX `idx_assign_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_assign_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修任务分配表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_assignment
-- ----------------------------
INSERT INTO `repair_assignment` VALUES (6, 4, 3, 1, '2025-11-30 19:47:31', 1, '', '2025-12-24 20:43:18');
INSERT INTO `repair_assignment` VALUES (11, 3, 3, 1, '2025-12-20 17:55:18', 0, '', '2025-12-20 17:55:18');
INSERT INTO `repair_assignment` VALUES (12, 3, 2, 0, '2025-12-23 18:31:29', 0, '', '2025-12-24 19:00:39');
INSERT INTO `repair_assignment` VALUES (15, 5, 2, 1, '2025-12-24 15:23:16', 1, '', '2025-12-24 20:39:54');
INSERT INTO `repair_assignment` VALUES (16, 5, 3, 0, '2025-12-24 15:26:49', 1, '', '2025-12-24 20:38:32');
INSERT INTO `repair_assignment` VALUES (17, 10, 3, 1, '2025-12-26 13:16:57', 1, '', '2025-12-29 15:16:15');
INSERT INTO `repair_assignment` VALUES (18, 24, 5, 1, '2025-12-28 20:50:56', 0, '', '2025-12-28 20:50:56');
INSERT INTO `repair_assignment` VALUES (19, 24, 3, 0, '2025-12-28 20:51:15', 0, '', '2025-12-28 20:53:00');

-- ----------------------------
-- Table structure for repair_evaluate
-- ----------------------------
DROP TABLE IF EXISTS `repair_evaluate`;
CREATE TABLE `repair_evaluate`  (
  `evaluate_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `request_id` bigint NOT NULL COMMENT '维修单ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价内容',
  `score` tinyint NOT NULL COMMENT '评分（1-5）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`evaluate_id`) USING BTREE,
  INDEX `fk_evaluate_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_evaluate_request`(`request_id` ASC) USING BTREE,
  CONSTRAINT `fk_evaluate_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_evaluate_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_score_range` CHECK (`score` between 1 and 5)
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '电脑维修评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_evaluate
-- ----------------------------
INSERT INTO `repair_evaluate` VALUES (4, 11, 10, '很好非常好', 5, '2025-12-29 16:10:50');

-- ----------------------------
-- Table structure for repair_log
-- ----------------------------
DROP TABLE IF EXISTS `repair_log`;
CREATE TABLE `repair_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志编号',
  `volunteer_id` bigint NOT NULL COMMENT '维修队员',
  `request_id` bigint NOT NULL COMMENT '报修单编号',
  `log_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修详细内容',
  `repair_duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修耗时（例如：2小时30分钟）',
  `solution_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解决方案摘要',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `fk_log_volunteer`(`volunteer_id` ASC) USING BTREE,
  INDEX `fk_log_request`(`request_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_log_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `volunteer_info` (`volunteer_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_log
-- ----------------------------
INSERT INTO `repair_log` VALUES (9, 3, 5, '测试维修内容', '1小时', '不知道', '2025-12-24 20:38:32');
INSERT INTO `repair_log` VALUES (10, 2, 5, '修修need', '1小时', 'dudududu max verstappen!!', '2025-12-24 20:39:54');
INSERT INTO `repair_log` VALUES (11, 3, 4, '主机接线已完成，但显示屏不亮', '1小时', '拔插显卡', '2025-12-24 20:43:18');
INSERT INTO `repair_log` VALUES (12, 3, 10, '1111', '111', '111', '2025-12-29 15:16:15');

-- ----------------------------
-- Table structure for repair_log_img
-- ----------------------------
DROP TABLE IF EXISTS `repair_log_img`;
CREATE TABLE `repair_log_img`  (
  `img_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片编号',
  `log_id` bigint NOT NULL COMMENT '关联的维修日志编号',
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维修过程图片URL',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`img_id`) USING BTREE,
  INDEX `idx_log_img_log_id`(`log_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_img_log` FOREIGN KEY (`log_id`) REFERENCES `repair_log` (`log_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修日志图片表（存放维修过程照片）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_log_img
-- ----------------------------
INSERT INTO `repair_log_img` VALUES (5, 9, 'repairLog/repairLog_9_img_1.jpg', '2025-12-24 20:38:32');
INSERT INTO `repair_log_img` VALUES (6, 10, 'repairLog/repairLog_10_img_1.jpg', '2025-12-24 20:39:54');

-- ----------------------------
-- Table structure for repair_request
-- ----------------------------
DROP TABLE IF EXISTS `repair_request`;
CREATE TABLE `repair_request`  (
  `request_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报修编号',
  `user_id` bigint NOT NULL COMMENT '报修人',
  `contact_type` enum('wechat','phone','email') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式类型（wechat: 微信, phone: 手机号, email: 邮箱）',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系账号或号码',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型',
  `device_system` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备系统',
  `device_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备型号',
  `problem_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '故障描述',
  `campus` tinyint(1) NOT NULL DEFAULT 0 COMMENT '校区（0: 南校区，1: 北校区）',
  `repair_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维修地点',
  `appointment_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约维修时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '报修状态（0:已提交待审核，1:审核通过，2:已被接收，3:已完成，4:已取消，5:用户自行解决，6:已被拒绝，7:已完成评价）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`request_id`) USING BTREE,
  INDEX `fk_request_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_request_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_request_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_request
-- ----------------------------
INSERT INTO `repair_request` VALUES (3, 10, 'wechat', 'lhx13712871903', 'mobile', 'android', 'xiaomi 12X', '换手机膜', 0, '三饭一', '周四下午', '', 2, '2025-11-16 17:40:05', NULL, '2025-12-20 17:55:18');
INSERT INTO `repair_request` VALUES (4, 10, 'phone', '13054453374', 'desktop', 'windows', '自组机', '主机接线已完成，但显示屏不亮', 0, '南苑13栋444', '周六全天', '', 3, '2025-11-16 17:43:27', NULL, '2025-12-24 20:43:18');
INSERT INTO `repair_request` VALUES (5, 10, 'wechat', 'lhx4656765', 'laptop', 'windows', '天选4', '测试', 0, '111', '22313', '', 3, '2025-11-17 15:46:35', NULL, '2025-12-24 20:39:54');
INSERT INTO `repair_request` VALUES (6, 10, 'email', '1521427714@qq.com', 'phone', 'Android', 'Xiaomi 12X', '屏幕损坏', 0, '一饭一', NULL, NULL, 0, '2025-12-12 10:58:15', NULL, '2025-12-12 10:58:15');
INSERT INTO `repair_request` VALUES (7, 17, 'email', '1521427714@qq.com', 'phone', 'Android', 'Xiaomi 12X', '屏幕损坏', 0, '一饭一', NULL, NULL, 0, '2025-12-12 12:01:35', NULL, '2025-12-12 12:01:35');
INSERT INTO `repair_request` VALUES (8, 17, 'email', 'user_2546281371@qq.com', 'phone', 'Android', 'Xiaomi 12X', '屏幕损坏', 0, '一饭一', NULL, NULL, 0, '2025-12-12 15:58:28', NULL, '2025-12-12 15:58:28');
INSERT INTO `repair_request` VALUES (9, 17, 'wechat', '18929983328', '1', '1', '1', 'test information\n ', 0, '1', NULL, NULL, 0, '2025-12-15 14:11:28', NULL, '2025-12-15 14:11:28');
INSERT INTO `repair_request` VALUES (10, 11, 'phone', '13054453374', 'desktop', 'windows', '自组机', '开机后键盘灯亮，但显示屏不亮', 0, '三饭一', '周三下午', '', 7, '2025-12-19 14:27:42', NULL, '2025-12-29 16:10:50');
INSERT INTO `repair_request` VALUES (11, 17, 'wechat', '测试号码', '', '测试系统', '测试设备', '测试问题', 0, '测试地址', NULL, NULL, 0, '2025-12-19 17:19:15', NULL, '2025-12-19 17:19:15');
INSERT INTO `repair_request` VALUES (12, 17, 'wechat', '1', '', '1', '1', '1', 0, '1', NULL, NULL, 0, '2025-12-19 17:20:33', NULL, '2025-12-19 17:20:33');
INSERT INTO `repair_request` VALUES (13, 17, 'wechat', '18929992393', '', '11', '1', '1', 0, '测试', NULL, NULL, 0, '2025-12-19 17:28:54', NULL, '2025-12-19 17:28:54');
INSERT INTO `repair_request` VALUES (14, 17, 'wechat', '1', '', '1', '', '测试', 0, '1', NULL, NULL, 0, '2025-12-19 17:31:05', NULL, '2025-12-19 17:31:05');
INSERT INTO `repair_request` VALUES (15, 17, 'wechat', '1', '', '1', '', '1', 0, '1', NULL, NULL, 0, '2025-12-19 17:32:25', NULL, '2025-12-19 17:32:25');
INSERT INTO `repair_request` VALUES (16, 17, 'wechat', '测试', '', '测试', '', '测试', 0, '测试', NULL, NULL, 0, '2025-12-19 17:50:50', NULL, '2025-12-19 17:50:50');
INSERT INTO `repair_request` VALUES (17, 17, 'wechat', '测试', '', '测试', '测试', '测试', 0, '测试', NULL, NULL, 0, '2025-12-19 19:46:33', NULL, '2025-12-19 19:46:33');
INSERT INTO `repair_request` VALUES (18, 17, 'wechat', '测试', '', '测试', '', ' 测试', 0, '测试', NULL, NULL, 0, '2025-12-19 19:51:23', NULL, '2025-12-19 19:51:23');
INSERT INTO `repair_request` VALUES (19, 17, 'wechat', '1', '', '1', '', '测试', 0, '1', NULL, NULL, 0, '2025-12-19 19:54:32', NULL, '2025-12-19 19:54:32');
INSERT INTO `repair_request` VALUES (20, 17, 'wechat', '1', '1', '1', '1', 'This is a history screen test', 0, '1', NULL, NULL, 1, '2025-12-25 12:04:33', NULL, '2025-12-26 13:22:16');
INSERT INTO `repair_request` VALUES (21, 17, 'wechat', '1', '1', '1', '', '测试', 0, '1', NULL, NULL, 1, '2025-12-25 22:33:10', NULL, '2025-12-26 13:22:14');
INSERT INTO `repair_request` VALUES (22, 17, 'wechat', '114514', '大份', '新日暮里os', '皮鼓', '仙贝家里沼气爆炸 请求雷利风行的维修', 0, '下北泽', NULL, NULL, 0, '2025-12-26 13:37:29', NULL, '2025-12-26 13:37:29');
INSERT INTO `repair_request` VALUES (23, 17, 'email', '1521427714@qq.com', 'phone', 'Android', 'Xiaomi 12X', '屏幕损坏', 0, '一饭一', '周一全天', '', 0, '2025-12-26 13:47:48', NULL, '2025-12-26 13:47:48');
INSERT INTO `repair_request` VALUES (24, 17, 'wechat', '1919810', '腚脑', '大皮鼓os', '不知道', '你是一个一个一个byd报错', 0, '女生宿舍', '1919年8月10号', '不知道填啥', 2, '2025-12-26 13:57:30', NULL, '2025-12-28 20:50:56');
INSERT INTO `repair_request` VALUES (25, 17, 'wechat', '1145141919810', '1', '1', '1', 'This is a test', 0, 'i dont know', '1', '1', 0, '2025-12-29 15:51:52', NULL, '2025-12-29 15:51:52');

-- ----------------------------
-- Table structure for repair_request_img
-- ----------------------------
DROP TABLE IF EXISTS `repair_request_img`;
CREATE TABLE `repair_request_img`  (
  `img_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片编号',
  `request_id` bigint NOT NULL COMMENT '关联的报修单编号',
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL地址',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`img_id`) USING BTREE,
  INDEX `idx_request_img_request_id`(`request_id` ASC) USING BTREE,
  CONSTRAINT `fk_request_img_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修请求图片表（每个报修单可对应多张故障图片）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repair_request_img
-- ----------------------------
INSERT INTO `repair_request_img` VALUES (6, 5, 'request/request_5_img_1.jpg', '2025-11-17 15:46:35');
INSERT INTO `repair_request_img` VALUES (7, 5, 'request/request_5_img_2.jpg', '2025-11-17 15:46:35');
INSERT INTO `repair_request_img` VALUES (8, 10, 'request/request_10_img_1.png', '2025-12-19 14:27:42');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（非微信注册可用）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码（非微信注册可用）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信OpenID（小程序内唯一）',
  `unionid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信UnionID（跨平台唯一，可选）',
  `wx_nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信昵称',
  `wx_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信头像URL',
  `role` enum('student','volunteer','admin','super_admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'student' COMMENT '角色类型：student, volunteer, admin, super_admin',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：0离线，1正常，2冻结',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login` datetime NULL DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表（支持微信登录）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (10, 'yuuhi0115', NULL, '刘同学', NULL, '1521427714@qq.com', 'user_10_avatar.jpg', NULL, NULL, NULL, NULL, 'student', 1, '2025-11-01 18:08:53', '2026-01-08 16:25:00', '2026-01-08 00:00:00');
INSERT INTO `users` VALUES (11, 'user_1274406252@qq.com', NULL, '结灯', NULL, '1274406252@qq.com', 'user_11_avatar.jpg', NULL, NULL, NULL, NULL, 'student', 1, '2025-11-07 18:28:19', '2025-12-29 15:15:51', '2025-12-29 00:00:00');
INSERT INTO `users` VALUES (12, 'user_13712871903', NULL, NULL, '13712871903', NULL, 'default_avatar.jpg', NULL, NULL, NULL, NULL, 'student', 1, '2025-11-11 14:49:02', '2025-11-20 19:01:15', '2025-11-11 14:49:02');
INSERT INTO `users` VALUES (13, 'superAdmin', NULL, '测试超管', NULL, '1521427714@qq.com', 'user_13_avatar.jpg', NULL, NULL, NULL, NULL, 'super_admin', 1, '2025-11-20 16:21:39', '2026-01-08 16:25:18', '2026-01-08 00:00:00');
INSERT INTO `users` VALUES (16, '微型校园接收机', NULL, '刘同学', NULL, '1274406252@qq.com', 'user_16_avatar.jpg', NULL, NULL, NULL, NULL, 'admin', 1, '2025-11-28 17:24:32', '2026-01-08 17:50:09', '2026-01-08 00:00:00');
INSERT INTO `users` VALUES (17, 'user_2546281371@qq.com', NULL, 'j同学', NULL, '2546281371@qq.com', 'user_17_avatar.jpg', NULL, NULL, NULL, NULL, 'student', 1, '2025-12-08 14:43:34', '2026-01-09 11:45:59', '2026-01-09 00:00:00');
INSERT INTO `users` VALUES (18, 'volunteer_2546281371@qq.com', NULL, '答哥', NULL, '2546281371@qq.com', 'user_18_avatar.jpg', NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-28 11:00:34', '2026-01-09 16:29:41', '2025-12-29 00:00:00');
INSERT INTO `users` VALUES (19, 'volunteer_1147529784@qq.com', NULL, '野兽后辈', NULL, '1147529784@qq.com', 'user_19_avatar.jpg', NULL, NULL, NULL, NULL, 'volunteer', 1, '2025-12-28 20:40:28', '2025-12-28 21:05:49', '2025-12-28 00:00:00');
INSERT INTO `users` VALUES (20, 'volunteer_eriana23333@gmail.com', NULL, '千早爱音', NULL, 'eriana23333@gmail.com', 'user_20_avatar.jpg', NULL, NULL, NULL, NULL, 'volunteer', 1, '2025-12-29 14:30:06', '2025-12-29 14:34:13', '2025-12-29 00:00:00');
INSERT INTO `users` VALUES (21, 'volunteer_aidealrays@gmail.com', NULL, '刘同学2', NULL, 'aidealrays@gmail.com', 'user_21_avatar.jpg', NULL, NULL, NULL, NULL, 'admin', 1, '2026-01-09 16:14:21', '2026-01-09 18:37:45', '2026-01-09 00:00:00');

-- ----------------------------
-- Table structure for volunteer_info
-- ----------------------------
DROP TABLE IF EXISTS `volunteer_info`;
CREATE TABLE `volunteer_info`  (
  `volunteer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID',
  `student_number` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号（11位数字，唯一）',
  `major_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '班级',
  `grade` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级（四位年份）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0: 退队 1: 正常 2: 退休',
  `contact_type` tinyint NULL DEFAULT NULL COMMENT '联系方式 0: 手机号 1: 邮箱号 2: 微信号 3: QQ号',
  `contact_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系号码',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`volunteer_id`) USING BTREE,
  UNIQUE INDEX `uk_student_number`(`student_number` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_volunteer_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿者信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of volunteer_info
-- ----------------------------
INSERT INTO `volunteer_info` VALUES (2, 16, '20231013693', '软件工程3班', '2019', 1, NULL, NULL, '2025-11-28 17:24:32', '2025-12-25 14:58:20');
INSERT INTO `volunteer_info` VALUES (3, 13, '20181696549', '软件工程5班', '2018', 1, 0, '19876912654', '2025-11-28 18:14:23', '2025-12-26 16:45:33');
INSERT INTO `volunteer_info` VALUES (4, 18, NULL, '计科1903', '2077', 1, NULL, NULL, '2025-12-28 11:00:34', '2025-12-28 20:49:34');
INSERT INTO `volunteer_info` VALUES (5, 19, NULL, '野兽班', '2003', 1, NULL, NULL, '2025-12-28 20:40:28', '2025-12-28 20:48:44');
INSERT INTO `volunteer_info` VALUES (6, 20, NULL, 'mygo', '2100', 1, NULL, NULL, '2025-12-29 14:30:06', '2025-12-29 14:34:13');
INSERT INTO `volunteer_info` VALUES (7, 21, '20227894215', '人工智能2班', '2022', 1, 0, '13698564982', '2026-01-09 16:14:21', '2026-01-09 16:19:47');

-- ----------------------------
-- Triggers structure for table repair_assignment
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_repair_assignment_update_time`;
delimiter ;;
CREATE TRIGGER `trg_repair_assignment_update_time` BEFORE UPDATE ON `repair_assignment` FOR EACH ROW BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table repair_request
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_repair_request_update_time`;
delimiter ;;
CREATE TRIGGER `trg_repair_request_update_time` BEFORE UPDATE ON `repair_request` FOR EACH ROW BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table users
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_users_update_time`;
delimiter ;;
CREATE TRIGGER `trg_users_update_time` BEFORE UPDATE ON `users` FOR EACH ROW BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
