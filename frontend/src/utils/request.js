import { request } from "@vtx/utils";
class Http {
  constructor() {}
  /**
   * get 请求
   * @param {*} url
   * @param {*} options {body}
   */
  get(url, options = {}) {
    const { body = {}, extraHeader = {}, msg = "" } = options;
    const headers = {};
    if (msg || extraHeader?.msg) {
      headers.operation = encodeURIComponent(
        JSON.stringify({
          menuId: sessionStorage.getItem("vtxmenuselectedKeys"),
          operation: extraHeader?.msg || msg,
        }),
      );
    }
    return request.get(url, {
      body,
      extraHeader: headers,
    });
  }

  /**
   * post 请求
   * @param {*} url
   * @param {*} options
   */
  post(url, options = {}) {
    const { body = {}, extraHeader = {}, msg = "" } = options;
    const headers = {};
    if (msg || extraHeader?.msg) {
      headers.operation = encodeURIComponent(
        JSON.stringify({
          menuId: sessionStorage.getItem("vtxmenuselectedKeys"),
          operation: extraHeader?.msg || msg,
        }),
      );
    }
    return request.post(url, {
      body,
      extraHeader: headers,
    });
  }
}

const http = new Http();
export default http;
