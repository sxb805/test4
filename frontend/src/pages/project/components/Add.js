/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { VtxFormLayout, VtxInput, VtxModal } from "@vtx/components";
import { Form, TreeSelect } from "antd";
import { projectService } from "../service";

function Add({ modalProps, formData = {}, confirm }) {
  const [form] = Form.useForm();
  const [staffTree, setStaffTree] = useState([]);
  const [staffNameMap, setStaffNameMap] = useState({});
  const authParams = {
    tenantId: new URLSearchParams(window.location.search).get("tenantId") || sessionStorage.getItem("tenantId") || "",
    userId: new URLSearchParams(window.location.search).get("userId") || sessionStorage.getItem("userId") || "",
  };

  const normalizeTree = (nodes = [], nameMap = {}) =>
    (Array.isArray(nodes) ? nodes : []).map((node) => {
      const rawValue = node?.attributes?.id || node?.key;
      const rawLabel = node?.name || rawValue;
      const isStaff = node?.type === "Staff";
      if (rawValue && isStaff) {
        nameMap[rawValue] = rawLabel;
      }
      return {
        ...node,
        value: rawValue,
        key: node?.key || rawValue,
        title: rawLabel,
        label: rawLabel,
        selectable: isStaff,
        disabled: !isStaff,
        children: normalizeTree(node?.children || node?.child || node?.nodes || [], nameMap),
      };
    });

  useEffect(() => {
    if (!modalProps.visible) {
      form.resetFields();
    }
  }, [modalProps.visible]);

  useEffect(() => {
    form.setFieldsValue({ ...formData });
  }, [formData, form]);

  useEffect(() => {
    projectService
      .loadStaffTree()
      .then((res) => {
        const map = {};
        const root = res?.data;
        const tree = root ? normalizeTree([root], map) : [];
        setStaffTree(tree);
        setStaffNameMap(map);
      })
      .catch(() => {
        setStaffTree([]);
        setStaffNameMap({});
      });
  }, []);

  const onFinish = (values) => {
    confirm && confirm({ ...values, tlName: staffNameMap[values?.tlId] || undefined });
  };

  return (
    <VtxModal {...modalProps} width={VtxModal.size.middle} onOk={() => form.submit()} forceRender>
      <Form form={form} onFinish={onFinish}>
        <VtxFormLayout cols={1}>
          <VtxFormLayout.Card title="项目信息">
            <VtxFormLayout.FormItem
              label="编号"
              name="code"
              rules={[
                { required: true, message: "必填" },
                { max: 32, message: "最大32个字符" },
                { pattern: /^[A-Za-z0-9_-]+$/, message: "仅支持字母、数字、下划线、中划线" },
              ]}
            >
              <VtxInput maxLength={32} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem
              label="名称"
              name="name"
              rules={[{ required: true, message: "必填" }, { max: 100, message: "最大100个字符" }]}
            >
              <VtxInput maxLength={100} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem
              label="TL"
              name="tlId"
              rules={[{ required: true, message: "必填" }]}
            >
              <TreeSelect
                allowClear
                showSearch
                treeNodeFilterProp="title"
                treeData={staffTree}
                placeholder="请选择TL"
                treeDefaultExpandAll
              />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
