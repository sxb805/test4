/**
 * 代理配置
 * https://umijs.org/zh-CN/config#proxy
 */
export default {
    '/api': {
        target: 'http://jsonplaceholder.typicode.com/',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
    },
    '/cloud': {
        target: 'http://ums.sxb805.cn:9001/',
        changeOrigin: true,
        // pathRewrite: { '^/cloud': '' },
    },
};
