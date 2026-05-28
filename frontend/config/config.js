import { defineConfig } from 'umi';
import routes from './routes';
import proxy from './proxy';

export default defineConfig({
        chainWebpack(config) {
                config
                    .entry('umi')
                    .prepend(require.resolve('@vtx/map/lib/VtxSearchCheckMap/style/css'))
                    .prepend(require.resolve('@vtx/map/lib/VtxSearchMap/style/css'));
        },

        // 配置路由
        routes,
        // 配置代理能力。
        proxy,
        // 配置 favicon 地址（href 属性）
        favicons: ['/favicon.ico'],
        // 开发环境下，可以保持组件状态
        fastRefresh: true,
        // 配置是否让生成的文件包含 hash 后缀，通常用于增量发布和避免浏览器加载缓存。
        hash: true,
        // https://umijs.org/zh-CN/config#history
        history: { type: 'hash' },
        manifest: {
            basePath: './',
        },
        dva: {},
        npmClient: 'pnpm',
        publicPath: process.env.NODE_ENV === 'production' ? './' : '/',
        // 解决编译报Variable @root-entry-name is undefined问题
        lessLoader: {
            modifyVars: {
                'root-entry-name': 'default'
            }
        },
    // 国际化插件
    // https://umijs.org/zh-CN/plugins/plugin-locale
    // 若开启，需创建src/locales/zh-CN.js文件来消除控制台的报警信息
    // locale: {
    //     default: 'zh-CN',
    //     antd: true,
    //     baseNavigator: false,
    // },
    // 解决react多实例问题
    mfsu: {
        shared: {
            react: {
                singleton: true,
                    requiredVersion: '^18.0.0'
            },
        },
    },
// https://umijs.org/zh-CN/config#cssloader
    cssLoaderModules: {
        exportLocalsConvention: 'camelCase',
    },
// https://umijs.org/docs/api/config#jsminifieroptions
// 生产环境移除console和debugger
    jsMinifierOptions: {
        drop: ['debugger']
    },
    scripts: [
    ],
    metas: [{ name: 'viewport', content: 'width=device-width, initial-scale=1' }],
        // 配置额外的 babel 插件。
        // https://umijs.org/zh-CN/config#extrababelplugins
        // 按需加载antd和lodash
    extraBabelPlugins: [
        // https://git.cloudhw.cn:3443/front-end/react-components
        [
            'import',
            {
                libraryName: '@ant-design/icons',
                customName: name => {
                    return `@ant-design/icons/lib/icons/${name}`;
                },
                camel2DashComponentName: false,
            },
            '@ant-design/icons',
        ],
        [
            'import',
            { libraryName: 'lodash', libraryDirectory: '', camel2DashComponentName: false },
            'lodash',
        ],
        ['import', { libraryName: '@vtx/utils', camel2DashComponentName: false }, '@vtx/utils'],
        // https://ahooks.js.org/zh-CN
        ['import', { libraryName: 'ahooks', camel2DashComponentName: false }, 'ahooks'],
        ["import", { "libraryName": "@vtx/ol-map", "style": "css" }, "@vtx/ol-map"],
        ['import', { libraryName: '@vtx/map', style: 'css', camel2DashComponentName: false }, '@vtx/map'],
    ],
    chainWebpack(memo) {
       return memo;
    },
    define: {
        'process.env.UMI_ENV': process.env.UMI_ENV,
        SDK_TYPES: '',
    },
    title: 'demo',
    routePrefetch: { },
    esbuildMinifyIIFE: true,
    headScripts: [{ src: './resources/lib/ICON_CONFIG.js' }]
});
