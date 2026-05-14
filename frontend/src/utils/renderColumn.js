import { cloneDeep } from "lodash-es";
import { isObject } from "lodash-es";
/**
 * 表格columns的数据处理
 * 传入[title, key, {...other}]
 * title必须，key必须
 */
export const renderColumns = (cols) => {
  return cols
    .map((item) => {
      let other = {};
      if (!!item[2] && isObject(item[2])) {
        other = cloneDeep(item[2]);
      }
      if ("display" in other) {
        if (!other.display) {
          return "";
        }
      }
      delete other.display;
      return {
        title: item[0],
        dataIndex: item[1],
        key: item[1],
        ...other,
        children: Array.isArray(other?.children)
          ? renderColumns(other.children)
          : undefined,
      };
    })
    .filter((item) => item);
};
/**
 * 为导出提供导出参数
 */
export const renderColumnParam = (cols) => {
  let colsParam = [];
  cols.map((item) => {
    let other = {};
    if (!!item[2] && isObject(item[2])) {
      other = cloneDeep(item[2]);
    }
    if ("display" in other) {
      if (!other.display) {
        return;
      }
    }
    if (item[1] === "action") {
      return;
    }
    colsParam.push({
      title: item[0],
      field: item[1],
      type: other.type,
    });
  });
  return JSON.stringify(colsParam);
};
export const renderColumnParamArr = (cols) => {
  let colsParam = [];
  cols.map((item) => {
    let other = {};
    if (!!item[2] && isObject(item[2])) {
      other = cloneDeep(item[2]);
    }
    if ("display" in other) {
      if (!other.display) {
        return;
      }
    }
    if (item[1] === "action") {
      return;
    }
    colsParam.push({
      title: item[0],
      key: item[1],
    });
  });
  return colsParam;
};
