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

 Date: 14/03/2026 23:46:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message`  (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint NOT NULL COMMENT '对话ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色 user / assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_conv_time`(`conversation_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 181 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session`  (
  `conversation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '对话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `headline` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对话标题/摘要',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '对话状态 1=进行中 0=结束',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`conversation_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge`;
CREATE TABLE `ai_knowledge`  (
  `knowledge_id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识编号',
  `source_type` tinyint NOT NULL COMMENT '来源类型：1=维修日志，2=社区帖子, 3=人工录入',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源ID（log_id / post_id）',
  `problem` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '故障描述',
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '解决方案',
  `embedding` blob NULL COMMENT '语义向量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`knowledge_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI智能问答知识库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_model_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_config`;
CREATE TABLE `ai_model_config`  (
  `model_id` int NOT NULL AUTO_INCREMENT,
  `model_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `model_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  PRIMARY KEY (`model_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '大模型配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_question_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_question_log`;
CREATE TABLE `ai_question_log`  (
  `question_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `matched_knowledge_id` bigint NULL DEFAULT NULL COMMENT '命中的知识ID',
  `similarity` double NULL DEFAULT NULL COMMENT '相似度',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI问答记录表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 238 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知已读记录表' ROW_FORMAT = DYNAMIC;

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
  UNIQUE INDEX `uk_comment_user_like`(`comment_id` ASC, `user_id` ASC) USING BTREE,
  CONSTRAINT `fk_post_comment_like` FOREIGN KEY (`comment_id`) REFERENCES `post_comment` (`comment_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 186 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

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
  INDEX `idx_post_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区帖子表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子一级评论表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子二级评论表' ROW_FORMAT = Dynamic;

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
  UNIQUE INDEX `uk_post_user_fav`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_fav_post`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_post_favorite` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子收藏表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子图片表' ROW_FORMAT = Dynamic;

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
  UNIQUE INDEX `uk_post_user_like`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_like_post`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_post_like` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子标签表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子标签关联表' ROW_FORMAT = Dynamic;

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
  INDEX `idx_view_post`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_post_view` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子浏览记录表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修任务分配表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '电脑维修评价表' ROW_FORMAT = Dynamic;

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
  `import_status` tinyint NOT NULL DEFAULT 0 COMMENT '是否录入知识库 0：否 1：是',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `fk_log_volunteer`(`volunteer_id` ASC) USING BTREE,
  INDEX `fk_log_request`(`request_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_request` FOREIGN KEY (`request_id`) REFERENCES `repair_request` (`request_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_log_volunteer` FOREIGN KEY (`volunteer_id`) REFERENCES `volunteer_info` (`volunteer_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修日志表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修请求表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修请求图片表（每个报修单可对应多张故障图片）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for reply_like
-- ----------------------------
DROP TABLE IF EXISTS `reply_like`;
CREATE TABLE `reply_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `reply_id` bigint NOT NULL COMMENT '二级评论ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_reply_user`(`reply_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_reply_id`(`reply_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_reply_id` FOREIGN KEY (`reply_id`) REFERENCES `post_comment_reply` (`reply_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '二级评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `follow_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关注记录ID',
  `follower_id` bigint NOT NULL COMMENT '关注者用户ID（谁关注）',
  `followee_id` bigint NOT NULL COMMENT '被关注者用户ID（关注谁）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '关注状态：1=已关注，0=已取消，2=悄悄关注',
  `is_update` tinyint(1) NOT NULL DEFAULT 0 COMMENT '被关注者是否更新',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`follow_id`) USING BTREE,
  UNIQUE INDEX `uk_follower_followee`(`follower_id` ASC, `followee_id` ASC) USING BTREE,
  INDEX `idx_follower`(`follower_id` ASC) USING BTREE,
  INDEX `idx_followee`(`followee_id` ASC) USING BTREE,
  CONSTRAINT `chk_no_self_follow` CHECK (`follower_id` <> `followee_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_profile_view
-- ----------------------------
DROP TABLE IF EXISTS `user_profile_view`;
CREATE TABLE `user_profile_view`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `viewer_id` bigint NOT NULL COMMENT '访问者用户ID',
  `viewed_user_id` bigint NOT NULL COMMENT '被访问用户ID',
  `viewer_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访问者IP',
  `view_count` int NOT NULL DEFAULT 1 COMMENT '累计访问次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次访问时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后访问时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_view`(`viewer_id` ASC, `viewed_user_id` ASC, `viewer_ip` ASC) USING BTREE,
  INDEX `idx_viewed_user`(`viewed_user_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户主页访问统计表' ROW_FORMAT = Dynamic;

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
  `user_signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `role` enum('student','volunteer','admin','super_admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'student' COMMENT '角色类型：student, volunteer, admin, super_admin',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：0离线，1正常，2冻结，3禁言',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login` datetime NULL DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表（支持微信登录）' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '志愿者信息表' ROW_FORMAT = Dynamic;

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
