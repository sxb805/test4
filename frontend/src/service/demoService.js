import http from '@/utils/request';

export const demoService = (() => {
    return {
        page: params => {
            return http.get('/demo', { body: params });
        },
    };
})();
