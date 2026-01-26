import { createRouter, createWebHistory } from 'vue-router'
import LoginView from "../view/LoginView.vue";
import MainView from "../view/MainView.vue";
import RegisterView from "../view/RegisterView.vue";
import UserBasicInfoView from "../view/user/BasicInfoView.vue";
import RepairFormView from "../view/repair/RepairFormView.vue";
import RepairHistoryView from "../view/repair/RepairHistoryView.vue";
import MemberManageView from "../view/admin/MemberManageView.vue";
import TaskListView from "../view/taskCenter/TaskListView.vue";
import messageCenterView from "../view/user/MessageCenterView.vue";
import MyTaskView from "../view/taskCenter/MyTaskView.vue";
import {checkToken} from "../api/userApi.js";
import {ElMessage} from "element-plus";
import Cookie from "js-cookie";
import CommunityCenterView from "../view/community/CommunityCenterView.vue";
import FollowListView from "../view/community/FollowListView.vue";
import MyFavoriteView from "../view/community/MyFavoriteView.vue";

// 定义路由
const routes = [
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/', component: MainView },
    { path: '/user/basicInfo', component: UserBasicInfoView },
    { path: '/user/messageCenter', component: messageCenterView },
    { path: '/repair/form', component: RepairFormView },
    { path: '/repair/history', component: RepairHistoryView},
    { path: '/admin/memberManage', component: MemberManageView },
    { path: '/taskCenter/list', component: TaskListView },
    { path: '/taskCenter/myTask', component: MyTaskView },
    {
        path: '/community',
        name: 'CommunityCenter',
        component: CommunityCenterView,
        props: true  // 启用路由props
    },
    { path: '/community/followList', component: FollowListView },
    { path: '/community/myFavorite', component: MyFavoriteView}
]

// 创建router实例
const router = createRouter({
    history: createWebHistory('/yixiu-site/'),
    routes
})

// 需要验证的路由白名单（或者定义哪些路由不需要验证）
const whiteList = ['/login', '/register']

// 全局前置守卫
router.beforeEach((to, from, next) => {
    if (!whiteList.includes(to.path)) {
        checkToken()
    }
    next()
})

export default router