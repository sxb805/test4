/*
 * @Author: yemu
 * @Date: 2021-03-30 22:27:16
 * @LastEditTime: 2022-01-15 16:42:47
 * @LastEditors: yewu
 * @Description: 按钮权限
 */
import http from "@/utils/request";
import { useEffect, useState } from "react";
import { useLocation } from "react-router";
import packageInfo from "../../package";

// 验证按钮权限接口路径
const FUNCTION_INTERFACE_PATH =
  "/cloud/management/api/v101/userAuth/hasFunctions";

function usePermission(codes = []) {
  const [permissions, setPermissions] = useState([]);
  let location = useLocation();
  const getFunctionCode = (code) => {
    const PREFIX = `CF_${packageInfo.name?.toUpperCase()}_${location?.pathname
      ?.replace("/", "")
      ?.toUpperCase()}`;
    return `${PREFIX}_${code.toUpperCase()}`;
  };

  // 请求接口服务，默认生产环境开启
  useEffect(() => {
    if (process.env.UMI_ENV === "prod") {
      if (codes.length > 0) {
        const functionCodes = codes
          .map((code) => getFunctionCode(code))
          .join(",");
        http
          .post(FUNCTION_INTERFACE_PATH, {
            body: { functionCodes },
          })
          .then((res) => {
            if (Array.isArray(res?.data) && res.data.length > 0) {
              setPermissions(res.data);
            }
          });
      }
    }
  }, []);

  // 验证
  const validate = (code) => {
    if (process.env.UMI_ENV !== "prod") {
      return true;
    } else {
      const functionCode = getFunctionCode(code);
      return permissions.includes(functionCode);
    }
  };

  return {
    validate,
  };
}

export default usePermission;
