import http from "@/utils/request";

const API_PREFIX = "/cloud/sample";
const URL = `${API_PREFIX}/taskWorkItem`;
const STAFF_TREE_URL = "/cloud/management/rest/tree/loadStaffTree";

export const taskWorkItemService = {
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
  weeklyOccupancy(params) {
    return http.get(`${URL}/weeklyOccupancy`, { body: params });
  },
  projectList(params) {
    return http.get(`${API_PREFIX}/project/list`, { body: params || {} });
  },
  loadStaffTree() {
    return http.get(STAFF_TREE_URL);
  },
};

export { API_PREFIX };
