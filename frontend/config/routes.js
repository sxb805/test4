/**
 * 路由
 * https://umijs.org/zh-CN/config#routes
 * https://umijs.org/zh-CN/docs/routing
 */
import MENU from '../menu.config';

const routes = [
    { path: '/demo', component: '@/pages/demo', title: '示例', parent: MENU.DEMO_BASIC },
    // 在这里添加路由
    {
        path: '/403',
        component: '@/pages/403',
        title: '403',
    },
    // 不可删除
    { path: '*', title: '404', component: '@/pages/404' },


]
export default [
    {
        path: '/',
        component: process.env.MENU ? '@/layouts/menu' : '@/layouts/index',
        routes: routes,
    },
].filter(item => item);
