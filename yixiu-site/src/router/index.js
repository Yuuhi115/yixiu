import { createRouter, createWebHistory } from 'vue-router'
import LoginView from "../view/LoginView.vue";
import HelloWorld from "../components/HelloWorld.vue";
import MainView from "../view/MainView.vue";
import RegisterView from "../view/RegisterView.vue";
import UserBasicInfoView from "../view/user/BasicInfoView.vue";
import RepairFormView from "../view/repair/RepairFormView.vue";
import RepairHistoryView from "../view/repair/RepairHistoryView.vue";
import MemberManageView from "../view/admin/MemberManageView.vue";

// 定义路由
const routes = [
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/about', component: HelloWorld },
    { path: '/', component: MainView },
    { path: '/user/basicInfo', component: UserBasicInfoView },
    { path: '/repair/form', component: RepairFormView },
    { path: '/repair/history', component: RepairHistoryView},
    { path: '/admin/memberManage', component: MemberManageView },
]

// 创建router实例
const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router