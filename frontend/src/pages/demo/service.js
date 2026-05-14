import http from "@/utils/request";

const URL = "/cloud/wfcs/web/api/companyuser";
const getTenantId = () =>
  new URLSearchParams(window.location.search).get("tenantId") ||
  sessionStorage.getItem("tenantId") ||
  undefined;

export const userService = {
  page(params) {
    return http.post(`${URL}/page`, {
      body: JSON.stringify(params),
    });
  },
  view(params) {
    return http.get(`${URL}/get`, {
      body: params,
    });
  },
  save(params) {
    return http.post(`${URL}/create`, {
      body: JSON.stringify({
        tenantId: getTenantId(),
        ...params,
      }),
    });
  },
  update(params) {
    return http.post(`${URL}/update`, {
      body: JSON.stringify({
        tenantId: getTenantId(),
        ...params,
      }),
    });
  },
  delete(params) {
    return http.post(`${URL}/delete`, {
      body: JSON.stringify(params),
    });
  },
  unlock(params) {
    return http.get(`${URL}/unlock`, {
      body: params,
    });
  },
};

export const getSourceTypeList = () =>
  http.get("/cloud/management/rest/np/param/getByParamTypeCode", {
    body: {
      parameters: JSON.stringify({
        paramTypeCode: "param_wfcs_company_user_source",
        tenantId: getTenantId(),
      }),
    },
  });
