import http from "@/utils/request";

const URL = "/cloud/sample/example";
const STAFF_TREE_URL = "/cloud/management/rest/tree/loadStaffTree";

export const exampleService = {
  page(params) {
    return http.get(`${URL}/page`, {
      body: params,
    });
  },
  view(params) {
    return http.get(`${URL}/get`, {
      body: params,
    });
  },
  save(params) {
    return http.post(`${URL}/save`, {
      body: JSON.stringify(params),
    });
  },
  update(params) {
    return http.post(`${URL}/update`, {
      body: JSON.stringify(params),
    });
  },
  delete(params) {
    return http.post(`${URL}/delete`, {
      body: params,
    });
  },
  loadStaffTree() {
    return http.get(STAFF_TREE_URL);
  },
};
