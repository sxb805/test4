/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { ExportOutlined } from "@ant-design/icons";
import { VtxModal } from "@vtx/components";
import { getVtxToken } from "@vtx/utils";
import { Button, Form, Input, message, Radio } from "antd";

const getToken = () => getVtxToken("token") || "";

const getDefaultFileName = (fileName) => {
  const time = new Date();
  const pad = (value) => `${value}`.padStart(2, "0");
  const stamp = `${time.getFullYear()}${pad(time.getMonth() + 1)}${pad(
    time.getDate(),
  )}${pad(time.getHours())}${pad(time.getMinutes())}${pad(time.getSeconds())}`;
  return fileName || `导出_${stamp}`;
};

const parseFileName = (disposition, fallbackName) => {
  if (!disposition) {
    return fallbackName;
  }
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }
  const normalMatch = disposition.match(/filename="?([^"]+)"?/i);
  if (normalMatch?.[1]) {
    return decodeURIComponent(normalMatch[1]);
  }
  return fallbackName;
};

const toQueryString = (params = {}) =>
  Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join("&");

const downloadByPostJson = ({ url, params, fallbackName }) =>
  new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const query = toQueryString(params);
    xhr.open("POST", query ? `${url}?${query}` : url, true);
    xhr.responseType = "blob";
    const token = getToken();
    if (token) {
      xhr.setRequestHeader("token", token);
      xhr.setRequestHeader("access_token", token);
      xhr.setRequestHeader("Authorization", `Bearer ${token}`);
    }
    xhr.onload = function onload() {
      if (xhr.status >= 200 && xhr.status < 300) {
        const blob = xhr.response;
        const disposition = xhr.getResponseHeader("Content-Disposition");
        const downloadName = parseFileName(disposition, fallbackName);
        const link = document.createElement("a");
        link.href = window.URL.createObjectURL(blob);
        link.download = downloadName;
        link.style.display = "none";
        document.body.appendChild(link);
        link.click();
        window.URL.revokeObjectURL(link.href);
        document.body.removeChild(link);
        resolve();
      } else {
        reject(new Error(`导出失败: ${xhr.status}`));
      }
    };
    xhr.onerror = function onerror() {
      reject(new Error("导出失败"));
    };
    xhr.send();
  });

function Export(props) {
  const {
    selectedRowKeys = [],
    tableData = [],
    params = {},
    requestExportUrl,
    columns = [],
    fileName,
    trigger,
  } = props;
  const [visible, setVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    if (visible) {
      form.setFieldsValue({
        type: "all",
        fileName: getDefaultFileName(fileName),
      });
    }
  }, [visible, fileName, form]);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const currentPageIds = tableData.map((item) => item.id).filter(Boolean);
    let exportIds = [];
    let downloadAll = false;

    if (values.type === "rows") {
      if (selectedRowKeys.length === 0) {
        message.warning("当前没有选中行");
        return;
      }
      exportIds = selectedRowKeys;
    }

    if (values.type === "page") {
      if (currentPageIds.length === 0) {
        message.warning("当前页无数据");
        return;
      }
      exportIds = currentPageIds;
    }

    if (values.type === "all") {
      if (tableData.length === 0) {
        message.warning("当前无数据");
        return;
      }
      downloadAll = true;
    }

    const payload = {
      ...params,
      downloadAll,
      ids: exportIds.join(","),
      fileName: values.fileName,
      ...(columns.length > 0
        ? {
            columnJson: JSON.stringify(
              columns.map((item) => ({
                title: item.title,
                field: item.field || item.key || item.dataIndex,
              })),
            ),
          }
        : {}),
    };

    setLoading(true);
    try {
      await downloadByPostJson({
        url: requestExportUrl,
        params: payload,
        fallbackName: `${values.fileName || "导出"}.xls`,
      });
      message.success("导出成功");
      setVisible(false);
    } catch (error) {
      message.error(error?.message || "导出失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {trigger ? React.cloneElement(trigger, { onClick: () => setVisible(true) }) : (
        <Button icon={<ExportOutlined />} onClick={() => setVisible(true)}>
          导出
        </Button>
      )}
      <VtxModal
        title="导出"
        visible={visible}
        width={VtxModal.size.small}
        maskClosable={false}
        onCancel={() => setVisible(false)}
        onOk={handleSubmit}
        confirmLoading={loading}
        destroyOnClose
        forceRender
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            type: "all",
            fileName: getDefaultFileName(fileName),
          }}
        >
          <Form.Item
            label="导出范围"
            name="type"
            rules={[{ required: true, message: "请选择导出范围" }]}
          >
            <Radio.Group>
              <Radio value="all">全部</Radio>
              <Radio value="rows">选中项</Radio>
              <Radio value="page">当前页</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item
            label="文件名"
            name="fileName"
            rules={[{ required: true, message: "请输入文件名" }]}
            style={{ marginBottom: 0 }}
          >
            <Input />
          </Form.Item>
        </Form>
      </VtxModal>
    </>
  );
}

export default Export;
