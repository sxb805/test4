/* global process */
import SDK from '@vtx/sdk';
import { getUrlParam } from '@vtx/utils';
import {matchRoutes} from 'umi'
import http from '@/utils/request';
import '@vtx/map/lib/VtxSearchMap/style/css';
import '@vtx/map/lib/VtxSearchCheckMap/style/css';
const sdk = new SDK({
    token: getUrlParam('token'),
});

window.$sdk = sdk;

const parseMapDefJson = (mapDefJson) => {
    try {
        const mapList = JSON.parse(mapDefJson || '[]');
        return Array.isArray(mapList) ? mapList.find(item => item?.defaultMap) || {} : {};
    } catch {
        return {};
    }
};

const normalizeMapInfo = (mapInfo = {}) => {
    const epsg = mapInfo?.epsg && mapInfo.epsg !== 'undefined' ? mapInfo.epsg : undefined;
    return {
        ...mapInfo,
        olCoverage: Array.isArray(mapInfo?.olCoverage) ? mapInfo.olCoverage : [],
        projection: mapInfo?.projection || (epsg ? `EPSG:${epsg}` : 'EPSG:4326'),
    };
};

const initMapInfo = () => {
    if (window.mapInfo) {
        return Promise.resolve();
    }
    window.mapInfo = normalizeMapInfo();
    return http.get('/casServer/user')
        .then(res => {
            if (res?.data?.mapDefJson) {
                window.mapInfo = normalizeMapInfo(parseMapDefJson(res.data.mapDefJson));
            }
        })
        .catch(() => {});
};

export function onRouteChange({ clientRoutes, location }) {
    const route = matchRoutes(clientRoutes, location.pathname)?.pop()?.route;
    if (route) {
        document.title = route.title || '';
    }
}

export function render(oldRender) {
    if (process.env.UMI_ENV !== 'prod') {
        initMapInfo().finally(oldRender);
        return;
    }
    let path = `${window.location.pathname}${window.location.hash?.split('?')[0]}`;
    // 必须: 规避前端iframe嵌套多层加/问题
    path = path.replace(/\/{2,}/g, '/');

    initMapInfo()
        .then(() => http.get(`/cloud/management/api/v101/userAuth/hasPath`, {
            body: {
                path: path,
            },
        }))
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
