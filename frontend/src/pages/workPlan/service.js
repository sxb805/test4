import http from "@/utils/request";

const API_PREFIX = "/cloud/sample";
const URL = `${API_PREFIX}/workPlan`;

export const workPlanService = {
  page(params) {
    return http.get(`${URL}/page`, { body: params });
  },
  view(params) {
    return http.get(`${URL}/get`, { body: params });
  },
  save(params) {
    return http.post(`${URL}/save`, { body: JSON.stringify(params) });
  },
  update(params) {
    return http.post(`${URL}/update`, { body: JSON.stringify(params) });
  },
  delete(params) {
    return http.post(`${URL}/delete`, { body: params });
  },
  projectList(params) {
    return http.get(`${API_PREFIX}/project/list`, { body: params || {} });
  },
};

export { API_PREFIX };
