/* eslint-disable react/prop-types */
import React from "react";
import { VtxFormLayout, VtxModal } from "@vtx/components";
import { Button, Form } from "antd";

function View(props) {
  const { modalProps, formData = {} } = props;
  const { onCancel } = modalProps;

  return (
    <VtxModal
      {...modalProps}
      width={VtxModal.size.middle}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          关闭
        </Button>,
      ]}
    >
      <Form layout="inline">
        <VtxFormLayout mode="view">
          <VtxFormLayout.Card title="项目信息">
            <VtxFormLayout.FormItem label="编号">{formData.code}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="名称">{formData.name}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="类型">{formData.typeName || formData.type}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="TL">{formData.tlName}</VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default View;
