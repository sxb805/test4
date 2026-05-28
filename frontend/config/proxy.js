/**
 * 代理配置
 * https://umijs.org/zh-CN/config#proxy
 */
export default {


    '/_BMapService': {
        target: 'http://ums.sxb805.cn:9001/',
        changeOrigin: true,
    },

    '/cloudFile': {
        target: 'http://ums.sxb805.cn:9001/',
        changeOrigin: true,
    },

    '/vortex': {
        target: 'http://ums.sxb805.cn:9001/',
        changeOrigin: true,
    },

    '/cas': {
            target: 'http://ums.sxb805.cn:9001/',
            changeOrigin: true,
        },
    '/casServer': {
            target: 'http://ums.sxb805.cn:9001/',
            changeOrigin: true,
        },

    '/api': {
        target: 'http://jsonplaceholder.typicode.com/',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
    },
    '/cloud/sample': {
        // target: 'http://ums.sxb805.cn:9001/',
        target: 'http://localhost:16666/',
        changeOrigin: true,
        // pathRewrite: { '^/cloud': '' },
    },

    '/cloud': {
            target: 'http://ums.sxb805.cn:9001/',
            changeOrigin: true,
            // pathRewrite: { '^/cloud': '' },
        },


};
