/*
 * @Author: yemu
 * @Date: 2021-04-02 15:43:14
 * @LastEditTime: 2022-02-24 20:01:47
 * @LastEditors: yemu
 * @Description: 生成按钮权限码和菜单JSON
 */
import fs from 'fs';
import path from 'path';
import yaml from 'js-yaml';
import groupBy from 'lodash/groupBy';
import routes from '../config/routes';
import { name } from '../package';

// 默认菜单配置路径
const MENU_SETTING_PATH = path.join(__dirname, '../menu.config.js');

(function generate() {
    // 基本路径
    const BASE = 'CF';
    // 主功能基本路径
    const FUNC_MAIN = 'FUNC_MAIN';
    // 包名大写
    const packageName = name.toUpperCase();
    // 内容
    const text = [
        '# 按钮权限功能码',
        '\r',
        '此文件是由脚本 permission2md.js 自动生成，权限码生成规则：CF\\_{ 工程包名 }\\_{ 页面路由 }\\_{ 操作类型 }',
        '\r',
    ];

    try {
        // 菜单配置
        let menuSetting = {};
        // 根菜单
        let rootMenu = {};
        // 判断根目录下是否存在菜单配置文件
        if (fs.existsSync(MENU_SETTING_PATH)) {
            menuSetting = require(MENU_SETTING_PATH).default;
            rootMenu = menuSetting.ROOT || {};
        }
        // 菜单配置
        const menus = [];
        // 遍历路由路径从头部注释中获取权限信息
        routes[0]?.routes
            .filter(item => !!item.title)
            .map(item => {
                // 读取路由component指向的文件地址并替换别名
                let filePath = item.component.replace('@', './src');
                // 判断是否是文件夹，如果是文件夹，路径默认加上index.js
                const stat = statSync(filePath);
                if (stat) {
                    if (stat.isDirectory()) {
                        filePath = `${filePath}/index.js`;
                    }
                } else {
                    filePath = `${filePath}.js`;
                }
                // 读取该文件路径内的内容
                const content = fs.readFileSync(filePath, 'utf8').toString();
                // 解析文件头部注释信息 获取按钮权限配置
                const { permissions } = transformer(content) || {};
                // 页面路由
                const page = item.path.replace('/', '').toUpperCase();
                // 记录功能权限
                const functions = [];
                if (Object.prototype.toString.call(permissions) === '[object Object]') {
                    // 页面标题 + 路由路径
                    text.push(`## ${item.title}(${item.path})`);
                    text.push('\r');
                    Object.keys(permissions).map(f => {
                        // 权限码生成规则：CF_{ 工程包名 }_{ 页面路由 }_{ 操作类型 }
                        const permissionCode = [BASE, packageName, page, f.toUpperCase()].join('_');
                        text.push(`-    ${permissionCode}  ${item.title}-${permissions[f]}`);
                        // 缓存功能权限
                        functions.push({
                            code: permissionCode,
                            name: permissions[f],
                        });
                    });
                    text.push('\r');
                }
                if (item.parent) {
                    // 父级CODE
                    const parentCode = item.parent.key;
                    menus.push({
                        // 菜单key，规则：{ 父级菜单CODE }_{ 页面路由大写 }
                        key: [parentCode, page].join('_'),
                        // 菜单名称
                        name: item.title,
                        // 父级菜单code
                        parentCode,
                        // 主功能 - 页面
                        mainFun: {
                            // 主功能CODE，唯一标识，规则：{ 父级菜单CODE }_{ 主功能基本路径 }_{ 页面路由大写 }
                            code: [parentCode, FUNC_MAIN, page].join('_'),
                            // 主功能标题
                            name: item.title,
                            // 页面访问地址
                            url: `/#${item.path}`,
                        },
                        // 子功能 - 按钮权限
                        functions,
                    });
                }
            });

        if (menus.length > 0) {
            // 将路由按父级菜单分组
            const groupMenus = groupBy(menus, 'parentCode');
            // 合并项目根节点
            const menuTree = {
                ...rootMenu,
                children: Object.keys(groupMenus).map(key => {
                    return {
                        ...menuSetting[key],
                        children: groupMenus[key],
                    };
                }),
            };
            // 将菜单写入到根目录menu.json中
            const data = JSON.stringify(menuTree, null, 4);
            fs.writeFileSync('menu.json', data);
            console.log('✨  菜单menu.json文件生成成功');
        }

        fs.writeFileSync('PERMISSION_CODE.md', text.join('\n'));
        console.log('✨  按钮权限码生成成功');
    } catch (error) {
        console.log(error);
        console.log(
            [
                '❌  按钮功能码生成出错，出错的原因可能是注释格式不正确，请仔细检查注释格式，确保其遵循yaml规范',
                '若您对yaml规范不太了解，您可以通过[YAML 1.2 规范](https://yaml.org/spec/1.2/spec.html)来学习',
                '当然您也可以通过阅读阮一峰老师的[YAML 语言教程](http://www.ruanyifeng.com/blog/2016/07/yaml.html)来学习',
            ].join('\n'),
        );
    }
})();

// 解析文件注释信息
function transformer(raw) {
    const [, comments = ''] = raw
        // clear head break lines
        .replace(/^\n\s*/, '')
        // split head comments & remaining code
        .match(/^(\/\*\*[^]*?\n\s*\*\/)?(?:\s|\n)*([^]+)?$/);

    const frontmatter = comments
        // clear / from head & foot for comment
        .replace(/^\/|\/$/g, '')
        // remove * from comments
        .replace(/(^|\n)\s*\*+/g, '$1');
    const parsed = frontmatter ? yaml.load(frontmatter) : {};
    const data = typeof parsed === 'object' ? parsed : {};
    return data;
}

function statSync(path) {
    try {
        return fs.statSync(path);
    } catch (error) {
        return false;
    }
}
