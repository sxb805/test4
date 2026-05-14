import { renderColumnParam, renderColumns } from "@/utils/renderColumn";
import { useAntdTable } from "ahooks";
import { Form, message } from "antd";
import { useEffect, useState } from "react";
import useDeleteRows from "./useDeleteRows";

/**
 *
 * @param act
 * @param dealFormData 额外处理formData的方法
 * @param pageUrl 分页函数名，默认为page
 * @param deleteUrl 删除函数名，默认为deleteItems
 * @param getCheckboxProps 选择框的默认属性配置
 * @param isSelection 表格是否显示选择框，默认显示
 * @param importURL 导入url
 * @param importTemplateURL 导入模板下载地址
 * @param exportUrl 导出url
 * @param exportColumnParam 导出参数
 * @param columnParam 表格列的配置描述
 * @param exportFileName 导出文件名
 * @param exportProp 导出额外的参数
 * @param rowButton 是否显示导出选中项， 默认为true
 * @param pageButton 是否显示导出当前页， 默认为true
 * @param manual useAntdTable是否需手动触发，默认为false,自动触发
 * @param errorURL 下载导入失败的文件的后台接口地址（必填）
 * @param errorDownloadName 下载导入失败的文件的文件名
 * @param importProp 导入额外的参数
 * @returns {{setImportVisible: (value: (((prevState: boolean) => boolean) | boolean)) => void, submit: () => void, selectedRowKeys: *[], deleteRows: {model: function(*): void, run: function(*): void}, pageSize: *, datagridProps: {[p: string]: any, onRowSelectionClear?: (function(): void), rowSelection?: {onChange(*, *): void, selectedRowKeys: [], getCheckboxProps: *}, startIndex: number, pagination: any, onChange: (pagination: any, filters?: any, sorter?: any) => void, onRefresh: () => void, toolbarTilte: string, columns, loading: boolean, dataSource: Data["list"]}, importProps: {visible: boolean, uploadURL, close: importProps.close, afterUpload: importProps.afterUpload, templateURL}, current: *, total: *, exportProps: {getExportParams(*): (null|{[p: string]: any, [p: number]: any, [p: symbol]: any, columnJson: *|string, fileName: *, downloadAll: boolean, ids: string, sort: string}), rowButton: boolean, pageButton: boolean, downloadURL, afterExport(*): void}, form: FormInstance<any>, reset: () => void, selectedRow: *[], importVisible: boolean}}
 */
export default ({
  act,
  dealFormData,
  pageUrl,
  deleteUrl,
  getCheckboxProps,
  isSelection = true,
  importURL,
  importTemplateURL,
  exportUrl,
  exportColumnParam,
  columnParam,
  exportFileName,
  exportProp = {},
  rowButton = true,
  pageButton = true,
  manual = false,
  errorURL,
  errorDownloadName,
  importProp = {},
}) => {
  const [form] = Form.useForm();
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRow, setSelectedRow] = useState([]);
  const [sorter, setSorter] = useState("");
  const [importVisible, setImportVisible] = useState(false);

  const getTableData = ({ current, pageSize, sorter }, formData) => {
    let sort;
    if (sorter && sorter.order) {
      const { order, field } = sorter;
      sort = `${field},${order === "descend" ? "DESC" : "ASC"}`;
    }
    let newFormData = dealFormData ? dealFormData(formData) : formData;
    setSorter(sort);
    return act(pageUrl ? pageUrl : "page", {
      formData: newFormData,
      page: current,
      size: pageSize,
      sort,
    });
  };
  const { refresh, tableProps, search, loading, run, params } = useAntdTable(
    getTableData,
    {
      defaultPageSize: 10,
      form,
      manual,
    },
  );
  const { current, pageSize, total } = tableProps?.pagination || {};
  const { submit, reset } = search;
  // 触发查询时清空列表选中项
  useEffect(() => {
    if (selectedRowKeys.length > 0) {
      setSelectedRowKeys([]);
      setSelectedRow([]);
    }
  }, [loading]);
  // 删除
  const deleteRows = useDeleteRows(
    (ids) => {
      return act(deleteUrl ? deleteUrl : "deleteItems", { ids }).then(
        (status) => {
          status && message.success("删除成功");
          return status;
        },
      );
    },
    {
      current: current || 1,
      pageSize: pageSize || 10,
      total: total || 0,
      formData: params[1],
      fetch: run,
    },
  );
  const columns = renderColumns(columnParam);
  const datagridProps = {
    ...tableProps,
    columns,
    toolbarTilte: document.title,
    startIndex: (current - 1) * pageSize + 1,
    ...(isSelection
      ? {
          rowSelection: {
            selectedRowKeys,
            onChange(selectedRowKeys, selectedRow) {
              setSelectedRowKeys(selectedRowKeys);
              setSelectedRow(selectedRow);
            },
            getCheckboxProps: getCheckboxProps,
          },
          onRowSelectionClear: () => {
            setSelectedRowKeys([]);
            setSelectedRow([]);
          },
        }
      : {}),
    onRefresh: refresh,
  };

  const exportProps = {
    downloadURL: exportUrl,
    rowButton,
    pageButton,
    getExportParams(exportType) {
      const { ...restSearchParams } = form.getFieldsValue();
      const commonParams = {
        ...(dealFormData ? dealFormData(restSearchParams) : restSearchParams),
        columnJson: renderColumnParam(exportColumnParam),
        fileName: exportFileName,
        sort: sorter,
      };
      const { dataSource } = tableProps;
      switch (exportType) {
        case "rows":
          if (selectedRowKeys.length === 0) {
            message.warning("当前没有选中行");
            return null;
          }
          return {
            ...commonParams,
            downloadAll: false,
            ids: selectedRowKeys.join(","),
          };
        case "page":
          if (dataSource.length === 0) {
            message.warning("当前页无数据");
            return null;
          }
          return {
            ...commonParams,
            downloadAll: false,
            ids: dataSource.map((item) => item.id).join(","),
          };
        case "all":
          if (total === 0) {
            message.warning("当前无数据");
            return null;
          }
          if (total > 10000) {
            message.warning(
              "系统导出数据上限为1万条，本次导出数据量已达到上限，超出的数据请重新筛选后分批进行导出",
            );
          }

          return {
            ...commonParams,
            downloadAll: true,
          };
        default:
          // 无逻辑
          break;
      }
      return {
        ...commonParams,
        downloadAll: true,
      };
    },
    ...(exportProp ? exportProp : {}),
    afterExport(status) {
      setSelectedRowKeys([]);
      setSelectedRow([]);
      status ? message.success("导出成功") : message.error("导出失败");
    },
  };

  const importProps = {
    visible: importVisible,
    uploadURL: importURL,
    templateURL: importTemplateURL,
    errorURL,
    errorDownloadName,
    close: () => {
      setImportVisible(false);
    },
    afterUpload: (dataStr) => {
      if (dataStr) {
        const data = JSON.parse(dataStr);
        if (data?.result === 0 && (!data.data || data.data.length === 0)) {
          message.success("导入成功");
          setImportVisible(false);
        } else {
          message.error("导入失败");
        }
        refresh();
      }
    },
    ...(importProp ? importProp : {}),
  };

  return {
    datagridProps,
    deleteRows,
    current,
    pageSize,
    total,
    submit,
    reset,
    form,
    exportProps,
    selectedRowKeys,
    importProps,
    importVisible,
    setImportVisible,
    selectedRow,
  };
};
