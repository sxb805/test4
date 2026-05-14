/* eslint-disable react/prop-types */
import React from "react";
import { VtxFormLayout, VtxModal } from "@vtx/components";
import { Button, Form } from "antd";
import dayjs from "dayjs";

function View(props) {
  const { modalProps, formData = {} } = props;
  const { onCancel } = modalProps;

  return (
    <VtxModal
      {...modalProps}
      width={VtxModal.size.small}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          关闭
        </Button>,
      ]}
    >
      <Form layout="inline">
        <VtxFormLayout mode="view">
          <VtxFormLayout.FormItem label="登录账号">
            {formData.username}
          </VtxFormLayout.FormItem>
          <VtxFormLayout.FormItem label="用户姓名">
            {formData.realName}
          </VtxFormLayout.FormItem>
          <VtxFormLayout.FormItem label="状态">
            {formData.stateName}
          </VtxFormLayout.FormItem>
          <VtxFormLayout.FormItem label="注册时间">
            {formData.registerDate ? dayjs(formData.registerDate).format("YYYY-MM-DD") : ""}
          </VtxFormLayout.FormItem>
          <VtxFormLayout.FormItem label="用户来源">
            {formData.sourceTypeValue}
          </VtxFormLayout.FormItem>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default View;
