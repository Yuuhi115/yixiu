<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { watchEffect, watch } from 'vue'
import { Star, ChatRound, Position, StarFilled, Promotion, ArrowDown, SortDown, SortUp, Search } from "@element-plus/icons-vue";
import { ElCollapseTransition } from 'element-plus';
import {
  getPostList,
  getAllPostTags,
  addPostLike,
  addPostFavorite,
  getCommentListByPostId,
  addComment,
  addReply,
  getReplyByCommentId, addCommentLike, addReplyLike, getPostListByFilter
} from '../api/communityApi.js'
import { ElMessage } from "element-plus"
import { formatTime } from "../utils/timeUtils.js";

// 定义 props，允许父组件传入自定义参数
const props = defineProps({
  initialFilters: {
    type: Object,
    default: () => ({})
  },
  enableTagFilter: {
    type: Boolean,
    default: true
  },
  enableSearch: {
    type: Boolean,
    default: true
  },
  enableSort: {
    type: Boolean,
    default: true
  }
})

// 标签数据
const tags = ref([])

// 动态列表
const postList = ref([])
const loading = ref(false)

// 滚动加载相关状态
const isLoadingMore = ref(false)  // 是否正在加载更多
const hasMore = ref(true)         // 是否还有更多数据

// 评论和回复加载状态
const commentLoadStates = reactive({
  loading: {},
  hasMore: {},
  currentPage: {}
})

const replyLoadStates = reactive({
  loading: {},
  hasMore: {},
  currentPage: {}
})

// 初始化时加载数据
onMounted(async () => {
  await loadAllTags()
  // 使用初始过滤参数更新过滤表单
  Object.assign(postFilterForm, props.initialFilters)
  await handlePostFilter()
})

// 传入的过滤参数
const postFilterForm = reactive({
  orderType: 'update_time',
  order: 'desc',
  tagId: '',
  postUserId: '',
  status: 0,
  keyword: '',
  ...props.initialFilters  // 合并传入的初始过滤参数
})

// 控制搜索框显示状态
const showSearchBar = ref(false)

// 排序选项
const orderOptions = [
  { value: 'update_time', label: '最新更新' },
  { value: 'favorite_num', label: '最多收藏' },
  { value: 'like_num', label: '最多点赞' },
  { value: 'comment_num', label: '最多评论' }
]

const buildPostQueryParams = () => {
  const params = {}
  if (postFilterForm.orderType !== '' && postFilterForm.orderType != null) {
    params.orderType = postFilterForm.orderType
  }
  if (postFilterForm.order !== '' && postFilterForm.order != null) {
    params.order = postFilterForm.order
  }
  if (postFilterForm.tagId !== '' && postFilterForm.tagId != null) {
    params.tagId = postFilterForm.tagId
  }
  if (postFilterForm.postUserId !== '' && postFilterForm.postUserId != null) {
    params.postUserId = postFilterForm.postUserId
  }
  if (postFilterForm.keyword !== '' && postFilterForm.keyword != null) {
    params.keyword = postFilterForm.keyword
  }
  return params
}

// 加载所有标签
const loadAllTags = async () => {
  const response = await getAllPostTags()
  if (response.code === 200) {
    tags.value = response.data
  }
}

const postPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 帖子评论分页参数
const commentPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})
// 帖子回复分页参数
const replyPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 加载初始帖子列表
const loadInitialPosts = async () => {
  loading.value = true
  postPagination.currentPage = 1  // 重置页码
  try {
    const params = buildPostQueryParams()
    params.pageNum = postPagination.currentPage
    params.pageSize = postPagination.pageSize

    const response = await getPostListByFilter(params)
    if (response.code === 200) {
      postList.value = response.data.list || []
      postPagination.total = response.data.total

      // 检查是否还有更多数据
      hasMore.value = postList.value.length < postPagination.total

      // 为每个帖子添加扩展状态
      postList.value.forEach(post => {
        post.expanded = false;
        post.showComments = false;
        post.comments = [];
        post.commentInput = '';
        post.loadingComments = false;
        post.showReplies = {};
        post.loadingReplies = {};
        post.replyLists = {};
        post.replySwitch = false;
      });
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

// 加载更多帖子
const loadMorePosts = async () => {
  if (isLoadingMore.value || !hasMore.value) return  // 防止重复加载

  isLoadingMore.value = true
  try {
    postPagination.currentPage++
    const params = buildPostQueryParams()
    params.pageNum = postPagination.currentPage
    params.pageSize = postPagination.pageSize

    const response = await getPostListByFilter(params)
    if (response.code === 200) {
      const morePosts = response.data.list || []
      postList.value = [...postList.value, ...morePosts]

      // 检查是否还有更多数据
      hasMore.value = postList.value.length < postPagination.total

      // 为新加载的帖子添加扩展状态
      for (let i = postList.value.length - morePosts.length; i < postList.value.length; i++) {
        const post = postList.value[i]
        if (!post.expanded) {
          post.expanded = false
          post.showComments = false
          post.comments = []
          post.commentInput = ''
          post.loadingComments = false
          post.showReplies = {}
          post.loadingReplies = {}
          post.replyLists = {}
          post.replySwitch = false
        }
      }
    }
  } catch (error) {
    console.error('加载更多帖子失败:', error)
    postPagination.currentPage--  // 加载失败时回退页码
    ElMessage.error('加载更多帖子失败')
  } finally {
    isLoadingMore.value = false
  }
}

const handlePostFilter = async () => {
  try {
    const params = buildPostQueryParams()
    params.pageNum = postPagination.currentPage
    params.pageSize = postPagination.pageSize

    const response = await getPostListByFilter(params)
    if (response.code === 200) {
      postList.value = response.data.list || []
      postPagination.total = response.data.total
      // 为每个帖子添加扩展状态
      postList.value.forEach(post => {
        post.expanded = false; // 默认不展开全部内容
        post.showComments = false  // 控制评论区显示
        post.comments = []         // 存储评论数据
        post.commentInput = ''     // 当前帖子的评论输入框
        post.loadingComments = false // 评论加载状态
        post.showReplies = {}      // 控制每条评论的回复显示状态
        post.loadingReplies = {}   // 控制每条评论的回复加载状态
        post.replyLists = {}       // 存储每条评论的回复列表
        post.replySwitch = false //控制显示发表评论或回复
      });
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const loadTaskListCondition = () => {
  if (postFilterForm.postUserId !== '' ||
      postFilterForm.tagId !== '' ||
      postFilterForm.status !== 0 ||
      postFilterForm.orderType !== 'update_time' ||
      postFilterForm.order !== 'desc' ||
      postFilterForm.keyword !== ''
  ) {
    handlePostFilter()
  } else {
    loadInitialPosts()
  }
}

const selectTag = (tagId) => {
  // 更新过滤条件中的标签ID
  postFilterForm.tagId = tagId
  handlePostFilter()
}

// 切换排序类型
const changeOrderType = (type) => {
  postFilterForm.orderType = type
  handlePostFilter()
}

// 切换排序方向
const toggleOrderDirection = () => {
  postFilterForm.order = postFilterForm.order === 'asc' ? 'desc' : 'asc'
  handlePostFilter()
}

// 搜索功能
const searchPosts = () => {
  handlePostFilter()
  // 搜索后隐藏搜索框
  showSearchBar.value = false
}

// 清空搜索
const clearSearch = () => {
  postFilterForm.keyword = ''
  handlePostFilter()
}

// 切换搜索框显示状态
const toggleSearchBar = () => {
  showSearchBar.value = !showSearchBar.value
  // 如果关闭搜索框且没有关键词，清空输入框
  if (!showSearchBar.value && !postFilterForm.keyword) {
    postFilterForm.keyword = ''
  }
}

// 获取当前排序类型的文本
const getOrderTypeLabel = computed(() => {
  const option = orderOptions.find(opt => opt.value === postFilterForm.orderType)
  return option ? option.label : '最新更新'
})

const loadComments = async (post, reset = true) => {
  if (reset) {
    post.comments = []
    commentLoadStates.currentPage[post.postId] = 1
    commentLoadStates.hasMore[post.postId] = true
  } else {
    commentLoadStates.currentPage[post.postId]++
  }
  const pageNum = commentLoadStates.currentPage[post.postId]
  try {
    let params = {
      postId: post.postId,
      pageNum: pageNum,
      pageSize: commentPagination.pageSize
    }
    const response = await getCommentListByPostId(params)
    if (response.code === 200) {
      const comments = response.data.list || []
      if (reset) {
        post.comments = comments
      } else {
        post.comments = [...post.comments, ...comments]
      }
      // 更新分页信息
      commentLoadStates.hasMore[post.postId] = post.comments.length < response.data.total
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
    if (!reset) {
      commentLoadStates.currentPage[post.postId]--  // 加载失败时回退页码
    }
  } finally {
    commentLoadStates.loading[post.postId] = false
  }
}

// 加载更多评论
const loadMoreComments = async (post) => {
  if (!commentLoadStates.loading[post.postId] && commentLoadStates.hasMore[post.postId]) {
    commentLoadStates.loading[post.postId] = true
    await loadComments(post, false)  // false 表示不重置，加载更多
  }
  commentLoadStates.loading[post.postId] = false
}

// 获取评论的回复
const loadReplies = async (post, comment, reset = true) => {
  const commentId = comment.commentId;

  if (!replyLoadStates.currentPage[commentId]) {
    replyLoadStates.currentPage[commentId] = 1
  } else if (!reset) {
    replyLoadStates.currentPage[commentId]++
  }
  const pageNum = replyLoadStates.currentPage[commentId]
  replyLoadStates.loading[commentId] = true;
  try {
    const params = {
      commentId: commentId,
      pageNum: pageNum,
      pageSize: replyPagination.pageSize
    };
    const response = await getReplyByCommentId(params);
    if (response.code === 200) {
      const replies = response.data.list || []
      if (!post.replyLists[commentId]) {
        post.replyLists[commentId] = replies
      } else if (reset) {
        post.replyLists[commentId] = replies
      } else {
        post.replyLists[commentId] = [...post.replyLists[commentId], ...replies]
      }

      // 更新分页信息
      replyLoadStates.hasMore[commentId] = post.replyLists[commentId].length < response.data.total
    } else {
      ElMessage.error(response.msg);
    }
  } catch (error) {
    console.error('加载回复失败:', error);
    ElMessage.error('加载回复失败');
    if (!reset) {
      replyLoadStates.currentPage[commentId]--  // 加载失败时回退页码
    }
  } finally {
    replyLoadStates.loading[commentId] = false;
  }
};

// 加载更多回复
const loadMoreReplies = async (post, comment) => {
  const commentId = comment.commentId;
  if (!replyLoadStates.loading[commentId] && replyLoadStates.hasMore[commentId]) {
    replyLoadStates.loading[commentId] = true
    await loadReplies(post, comment, false)  // false 表示不重置，加载更多
  }
  replyLoadStates.loading[commentId] = false;
}

// 切换回复显示状态
const toggleReplies = async (post, comment) => {
  const commentId = comment.commentId;

  // 如果还没有加载过回复，则先加载
  if (!post.replyLists[commentId] && !replyLoadStates.loading[commentId]) {
    await loadReplies(post, comment);
  }

  // 切换显示状态
  post.showReplies[commentId] = !post.showReplies[commentId];
};

const getSubmitCommentButtonText = (post) => {
  return post.replySwitch ? '回复' : '发表评论'
}
const getSubmitCommentPlaceholder = (post) => {
  return post.replySwitch ? '请输入回复...' : '请输入评论...'
}

const goToComment = async (post) => {
  // 切换评论区显示状态
  if (post.showComments) {
    post.showComments = false
    return
  }

  // 显示评论区并加载评论
  post.showComments = true
  commentLoadStates.loading[post.postId] = true
  try {
    await loadComments(post)
  } finally {
    commentLoadStates.loading[post.postId] = false  // 确保加载状态被正确关闭
  }
}

// 提交评论函数
const submitComment = async (post, parentCommentId = null, replyToUserId = null) => {
  if (!post.commentInput.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const commentData = {
      postId: post.postId,
      content: post.commentInput,
    }

    // 如果是通过回复按钮触发的，使用存储的回复信息
    if (post.replyingTo) {
      parentCommentId = post.replyingTo.parentCommentId;
      replyToUserId = post.replyingTo.replyToUserId;
      delete post.replyingTo; // 清除临时信息
    }

    // 如果是回复，则调用回复接口
    if (post.replySwitch) {
      commentData.commentId = parentCommentId
      commentData.toUserId = replyToUserId
      const response = await addReply(commentData)
      if (response.code === 200) {
        ElMessage.success('回复成功')
        post.commentInput = ''

        // 更新回复列表
        await loadComments(post)
        const comment = post.comments.find(c => c.commentId === parentCommentId);
        if (comment) {
          await loadReplies(post, comment);
        } else {
          console.error('未找到对应的评论:', parentCommentId);
        }

        // 如果该评论的回复已经显示，则刷新显示
        if (post.showReplies[parentCommentId]) {
          post.showReplies[parentCommentId] = false;
          await nextTick();
          post.showReplies[parentCommentId] = true;
        } else {
          post.showReplies[parentCommentId] = true;
        }
        // 更新帖子的评论数
        post.commentNum += 1
        post.replySwitch = false
      } else {
        ElMessage.error(response.msg)
      }
    } else {
      // 普通评论
      const response = await addComment(commentData)
      if (response.code === 200) {
        ElMessage.success('评论成功')
        post.commentInput = ''
        // 重新加载评论
        await loadComments(post)

        // 更新帖子的评论数
        post.commentNum += 1
      } else {
        ElMessage.error(response.msg)
      }
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  }
};

// 回复某条评论
const replyToComment = (post, comment) => {
  if (!post.replySwitch) {
    post.replySwitch = true
  }
  // 设置回复的逻辑
  post.replyingTo = {
    parentCommentId: comment.commentId,
    replyToUserId: comment.userId
  };
};

// 回复某人的回复
const replyToReply = (post, comment, reply) => {
  if (!post.replySwitch) {
    post.replySwitch = true
  }
  // 设置回复二级评论的逻辑
  post.commentInput = `@${reply.fromUserName} `;
  post.replyingTo = {
    parentCommentId: comment.commentId,
    replyToUserId: reply.fromUserId,
    parentReplyId: reply.replyId
  };
};

// 取消回复
const cancelReply = (post) => {
  post.replySwitch = false
  post.commentInput = ''
}

// 添加评论点赞函数
const toggleLikeComment = async (comment) => {
  if (comment.isLike === 0) {
    comment.likeNum += 1;
    comment.isLike = 1;
  } else {
    comment.likeNum -= 1;
    comment.isLike = 0;
  }
  const response = await addCommentLike(comment.commentId)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
  }
}

// 添加回复点赞函数
const toggleLikeReply = async (reply) => {
  try {
    if (reply.isLike === 0) {
      reply.likeNum += 1;
      reply.isLike = 1;
    } else {
      reply.likeNum -= 1;
      reply.isLike = 0;
    }
    const response = await addReplyLike(reply.replyId)
    if (response.code !== 200) {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    console.error('点赞失败:', error);
    ElMessage.error('点赞失败');
  }
};

// 检查是否应该显示展开按钮
const shouldShowExpandButton = (post) => {
  if (!post.content) return false;
  const lines = post.content.split('\n').length + Math.floor(post.content.length / 50); // 粗略估算行数
  return lines > 2;
};

// 切换展开/收起状态
const toggleExpand = (post) => {
  post.expanded = !post.expanded;
};

const toggleLike = async (post) => {
  if (post.isLiked === 0) {
    post.likeNum += 1
    post.isLiked = 1
  } else {
    post.likeNum -= 1
    post.isLiked = 0
  }
  const response = await addPostLike(post.postId)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
  }
}

const toggleFavorite = async (post) => {
  if (post.isFavorite === 0) {
    post.favoriteNum += 1
    post.isFavorite = 1
  } else {
    post.favoriteNum -= 1
    post.isFavorite = 0
  }
  const response = await addPostFavorite(post.postId)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
  }
}
</script>

<template>
  <div>
    <!-- 分区导航 -->
    <div class="section-nav-menu">
      <div class="tag-buttons">
        <el-button
            :type="!postFilterForm.tagId ? 'primary' : 'default'"
            @click="selectTag('')"
            size="default"
        >
          全部
        </el-button>
        <el-button
            v-for="tag in tags.slice(0, 6)"
            :key="tag.tagId"
            :type="postFilterForm.tagId === tag.tagId.toString() ? 'primary' : 'default'"
            @click="selectTag(tag.tagId.toString())"
            size="default"
        >
          {{ tag.tagName }}
        </el-button>
      </div>
      <div class="order-filter">
        <!-- 搜索图标 -->
        <el-button
            size="small"
            @click="toggleSearchBar"    style="border: 0;border-radius: 25px"
        >
          <el-icon>
            <Search />
          </el-icon>
        </el-button>

        <!-- 弹出搜索框 -->
        <transition name="slide-down">
          <div v-show="showSearchBar" class="popup-search-bar">
            <el-input
                v-model="postFilterForm.keyword"
                placeholder="搜索帖子标题或内容..."
                clearable
                @keyup.enter="searchPosts"        style="width: 200px; margin-right: 8px;"
            />
            <el-button @click="searchPosts" size="small">搜索</el-button>
            <el-button @click="clearSearch" size="small" v-if="postFilterForm.keyword">清空</el-button>
          </div>
        </transition>

        <el-dropdown @command="changeOrderType" trigger="click">
          <el-button size="small" style="border: 0">
            {{ getOrderTypeLabel }}
            <el-icon>
              <arrow-down/>
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                  v-for="option in orderOptions"
                  :key="option.value"
                  :command="option.value"
                  :class="{ 'is-active': postFilterForm.orderType === option.value }"
              >
                {{ option.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button
            size="small"
            @click="toggleOrderDirection" style="border: 0"
        >
          <el-icon v-if="postFilterForm.order === 'desc'">
            <sort-down/>
          </el-icon>
          <el-icon v-else>
            <sort-up/>
          </el-icon>
          {{ postFilterForm.order === 'desc' ? '降序' : '升序' }}
        </el-button>
      </div>
    </div>

    <!-- 动态列表 -->
    <div class="post-list">
      <div v-if="postList && postList.length > 0">
        <div
            v-for="(post, index) in postList"
            :key="index"
            class="post-item"
        >
          <div class="user-header">
            <el-avatar :size="40" :src="post.avatar"/>
            <div class="user-details">
              <div class="username">{{ post.username }}</div>
              <div class="time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>

          <div class="post-detail">
            <div class="title_tags">
              <div class="title">
                <p>{{ post.title }}</p>
              </div>
              <div class="tags">
                <el-tag
                    v-for="tag in post.tags"
                    :key="tag.tagId"
                    size="small"
                    type="primary"
                    style="margin-right: 10px;"
                >
                  {{ tag.tagName }}
                </el-tag>
              </div>
            </div>
            <div class="content">
              <div
                  class="content-text"
                  :class="{ expanded: post.expanded }"
                  :style="post.expanded ? {} : {'-webkit-line-clamp': 2}"
              >
                <p align="left">{{ post.content }}</p>
              </div>
              <div
                  v-if="shouldShowExpandButton(post)"
                  class="expand-btn"
                  @click="toggleExpand(post)"
              >
                {{ post.expanded ? '收起' : '展开' }}
              </div>
            </div>
          </div>

          <!-- 图片展示 -->
          <div v-if="post.imgUrls && post.imgUrls.length" class="images-grid">
            <el-image
                v-for="(img, imgIndex) in post.imgUrls.slice(0, 9)"
                :key="imgIndex"
                :src="img"
                fit="cover"
                class="post-image"
                :preview-src-list="post.imgUrls.map(i => i)"
                :initial-index="imgIndex"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="actions">
            <el-button type="text" size="large" @click="toggleFavorite(post)">
              <el-icon v-if="post.isFavorite === 0">
                <Star/>
              </el-icon>
              <el-icon v-else>
                <StarFilled/>
              </el-icon>
              {{ post.favoriteNum || 0 }}
            </el-button>
            <el-button type="text" size="large" @click="goToComment(post)">
              <el-icon>
                <ChatRound/>
              </el-icon>
              {{ post.commentNum || 0 }}
            </el-button>
            <el-button type="text" size="large" @click="toggleLike(post)">
              <el-icon v-if="post.isLiked === 0">
                <Position/>
              </el-icon>
              <el-icon v-else>
                <Promotion/>
              </el-icon>
              {{ post.likeNum || 0 }}
            </el-button>
          </div>

          <!-- 评论区 -->
          <el-collapse-transition>
            <div v-if="post.showComments" class="comment-section">
              <!-- 评论输入框 -->
              <div class="comment-input-section">
                <el-input
                    v-model="post.commentInput"
                    :rows="3"
                    type="textarea"
                    :placeholder="getSubmitCommentPlaceholder(post)"
                    maxlength="200"
                    show-word-limit
                />
                <div class="submit-comment-btn">
                  <el-button v-if="post.replySwitch === true" type="danger" @click="cancelReply(post)">取消回复
                  </el-button>
                  <el-button type="primary" @click="submitComment(post)">{{ getSubmitCommentButtonText(post) }}
                  </el-button>
                </div>
              </div>

              <!-- 评论列表 -->
              <div class="comments-list">
                <div v-if="post.loadingComments" class="loading-comments">
                  <el-skeleton :rows="3" animated/>
                </div>
                <div
                    v-else-if="post.comments.length === 0"
                    class="no-comments"
                >
                  暂无评论
                </div>
                <div
                    v-else
                    v-for="comment in post.comments"
                    :key="comment.commentId"
                    class="comment-item"
                >
                  <div class="comment-header">
                    <el-avatar :size="30" :src="comment.avatar"/>
                    <div class="comment-user-info">
                      <div class="comment-username">
                        <span style="margin-right: 10px">{{ comment.username }}</span>
                        <el-tag v-if="comment.userId === post.userId" size="small" type="success">作者</el-tag>
                      </div>
                      <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                    </div>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <div style="margin-left: 20px">
                      <el-button
                          v-if="comment.replyNum > 0"
                          type="text"
                          size="small"
                          @click="toggleReplies(post, comment)"
                      >
                        {{ post.showReplies[comment.commentId] ? '收起' : '查看回复' }}({{ comment.replyNum }})
                      </el-button>
                    </div>
                    <div>
                      <el-button type="text" size="small" @click="toggleLikeComment(comment)">
                        <el-icon v-if="comment.isLike === 0">
                          <Position/>
                        </el-icon>
                        <el-icon v-else>
                          <Promotion/>
                        </el-icon>
                        {{ comment.likeNum || 0 }}
                      </el-button>
                      <el-button type="text" size="small" @click="replyToComment(post, comment)">回复</el-button>
                    </div>
                  </div>
                  <!-- 回复列表 -->
                  <div
                      v-if="post.showReplies[comment.commentId]"
                      class="replies-container"
                  >
                    <div v-if="post.loadingReplies[comment.commentId]" class="loading-replies">
                      <el-skeleton :rows="2"/>
                    </div>
                    <div
                        v-else
                        v-for="reply in post.replyLists[comment.commentId] || []"
                        :key="reply.replyId"
                        class="reply-item"
                    >
                      <div class="reply-header">
                        <el-avatar :size="24" :src="reply.fromUserAvatar"/>
                        <div class="reply-user-info">
                          <div class="reply-username">
                            <span style="font-weight: bold;">{{ reply.fromUserName }}</span>
                            <span style="color: #999; margin: 0 5px;">回复</span>
                            <span style="font-weight: bold;">{{ reply.toUserName }}</span>
                            <el-tag v-if="reply.fromUserId === post.userId" size="small" type="success"
                                    style="margin-left: 5px;">作者
                            </el-tag>
                          </div>
                          <div class="reply-time">{{ formatTime(reply.createTime) }}</div>
                        </div>
                      </div>
                      <div class="reply-content">{{ reply.content }}</div>
                      <div class="reply-actions">
                        <el-button type="text" size="small" @click="toggleLikeReply(reply)">
                          <el-icon v-if="reply.isLike === 0">
                            <Position/>
                          </el-icon>
                          <el-icon v-else>
                            <Promotion/>
                          </el-icon>
                          {{ reply.likeNum || 0 }}
                        </el-button>
                        <el-button type="text" size="small" @click="replyToReply(post, comment, reply)">回复</el-button>
                      </div>
                    </div>
                    <!-- 加载更多回复按钮 -->
                    <div v-if="replyLoadStates.hasMore[comment.commentId] && !replyLoadStates.loading[comment.commentId]"
                         class="load-more-container-reply">
                      <el-button type="text" @click="loadMoreReplies(post, comment)" class="load-more-btn-reply">
                        点击加载更多回复
                      </el-button>
                    </div>

                    <!-- 回复加载中提示 -->
                    <div v-if="replyLoadStates.loading[comment.commentId]" class="loading-replies">
                      <el-skeleton :rows="1" />
                    </div>
                  </div>
                </div>
                <!-- 加载更多评论按钮 -->
                <div v-if="commentLoadStates.hasMore[post.postId] && !commentLoadStates.loading[post.postId]"
                     class="load-more-container-comment">
                  <el-button type="text" @click="loadMoreComments(post)" class="load-more-btn-comment">
                    点击加载更多评论
                  </el-button>
                </div>
                <!-- 评论加载中提示 -->
                <div v-if="commentLoadStates.loading[post.postId]" class="loading-comments">
                  <el-skeleton :rows="2" animated />
                </div>
              </div>
            </div>
          </el-collapse-transition>
        </div>
      </div>
      <div v-else-if="!loading" class="no-posts">
        <el-empty description="暂无帖子"/>
      </div>
      <!-- 加载更多 -->
      <div v-if="loading" class="loading-more">
        <el-skeleton :rows="4" animated/>
      </div>
    </div>
  </div>
</template>



<style scoped>
.section-nav-menu {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 8px;
  padding: 10px 20px;
  margin-bottom: 10px;
  margin-left: auto;
  margin-right: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
  width: 760px;
}

.tag-buttons {
  display: flex;
  justify-content: flex-start;
}

.tag-buttons .el-button {
  border: 0;
}

.post-list {
  width: 800px;
  margin: 0 auto;
}

.no-posts {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 16px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
  width: 760px;
  margin: 0 auto 10px;
}

.order-filter {
  position: relative;  /* 添加相对定位 */
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.order-filter .el-button {
  border: 1px solid #dcdfe6;
}

.order-filter .el-button.is-active {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: white;
}

.popup-search-bar {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: flex;
  align-items: center;
  min-width: 250px;
}

.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease;
  transform: translateY(0);
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.post-item {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-details .username {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 2px;
}

.user-details .time {
  font-size: 12px;
  color: #999;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  margin-bottom: 15px;
}

.post-image {
  width: 100%;
  height: 120px;
  border-radius: 4px;
  cursor: pointer;
}

.actions {
  display: flex;
  justify-content: space-around;
  gap: 20px;
  border-top: 1px solid #f5f5f5;
  padding-top: 15px;
}

.actions .el-button {
  width: 200px;
  color: #409EFF;
  border-radius: 10px; /* 圆角按钮 */
  padding: 8px 16px; /* 调整内边距 */
  font-size: 20px;
}

.actions .el-button .el-icon {
  margin-right: 5px;
}

.actions .el-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  color: white;
}

/* 当按钮处于激活状态时的样式 */
.actions .el-button.is-active {
  background-color: #3a8ee6;
  border-color: #3a8ee6;
}

.loading-more {
  margin-top: 20px;
}

.user-details {
  padding-left: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.post-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.post-detail .title {
  font-size: 16px;
  font-weight: bold;
}

.content {
  margin-bottom: 15px;
  line-height: 1.4;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  margin-bottom: 8px;
}

.content-text.expanded {
  -webkit-line-clamp: unset;
  overflow: visible;
  display: block;
}

.expand-btn {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
}

.title_tags {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* 评论模块 */
.comment-section {
  margin-top: 15px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 15px;
  overflow: hidden;
  max-height: 500px;
}

.comment-input-section {
  text-align: right;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.submit-comment-btn {
  text-align: right;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.comments-list {
  margin-top: 10px;
  min-height: 20px;
  transition: all 0.3s ease;
  max-height: 250px;
  overflow-y: auto;
  padding-right: 8px;
  padding-bottom: 5px; /* 添加底部内边距 */
  box-sizing: border-box; /* 确保内边距不影响尺寸计算 */
}

/* 滚动条样式优化 */
.comments-list::-webkit-scrollbar {
  width: 6px;
}

.comments-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.comments-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.comments-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  transition: all 0.3s ease;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user-info {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 10px;
}

.comment-username {
  display: flex;
  align-items: center;
  font-weight: bold;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  display: flex;
  justify-content: flex-start;
  margin-left: 40px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 10px 0;
}

.loading-comments {
  padding: 10px 0;
}

/* 回复模块 */
.replies-container {
  margin-left: 40px;
  margin-top: 10px;
  padding-left: 15px;
  border-left: 2px solid #eaeaea;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.reply-user-info {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 10px;
}

.reply-username {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-content {
  text-align: left;
  margin-left: 34px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 5px;
  font-size: 14px;
}

.reply-actions {
  margin-left: 34px;
  text-align: left;
  font-size: 12px;
}

.reply-actions .el-button {
  padding: 2px 4px;
  margin-right: 10px;
}

.loading-replies {
  margin-left: 34px;
  padding: 10px 0;
}
.load-more-container-comment {
  text-align: center;
  padding: 10px 0;
}

.load-more-btn-comment {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more-btn-comment:hover {
  color: #66b1ff;
  transform: translateY(-1px);
}

.load-more-container-reply {
  text-align: center;
  padding: 8px 0;
  margin-left: 34px;
}

.load-more-btn-reply {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more-btn-reply:hover {
  color: #66b1ff;
}
</style>
