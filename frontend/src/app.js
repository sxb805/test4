import SDK from '@vtx/sdk';
import { getUrlParam } from '@vtx/utils';
import {matchRoutes} from 'umi'
import http from '@/utils/request';
const sdk = new SDK({
    token: getUrlParam('token'),
});

window.$sdk = sdk;

export function onRouteChange({ clientRoutes, location }) {
    const route = matchRoutes(clientRoutes, location.pathname)?.pop()?.route;
    if (route) {
        document.title = route.title || '';
    }
}

export function render(oldRender) {
    if (process.env.UMI_ENV !== 'prod') {
        oldRender();
        return;
    }
    let path = `${window.location.pathname}${window.location.hash?.split('?')[0]}`;
    // 必须: 规避前端iframe嵌套多层加/问题
    path = path.replace(/\/{2,}/g, '/');

    http.get(`/cloud/management/api/v101/userAuth/hasPath`, {
        body: {
            path: path,
        },
    })
        .then(res => {
            if (res?.result == 0) {
                if (typeof res?.data == 'boolean') {
                    if (res?.data) {
                        oldRender();
                    } else {
                        location.href = `${location.pathname}#/403`;
                        oldRender();
                    }
                } else {
                    oldRender();
                }
            } else {
                oldRender();
            }
        })
        .catch(() => {
            oldRender();
        });
}
