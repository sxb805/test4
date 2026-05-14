/* eslint-disable react/prop-types */
import React, { useEffect } from "react";
import { VtxFormLayout, VtxInput, VtxModal } from "@vtx/components";
import { Checkbox, DatePicker, Form, InputNumber } from "antd";
import dayjs from "dayjs";

const toDayjs = (value) => {
  if (!value) {
    return undefined;
  }
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed : undefined;
};

function Add({ modalProps, formData = {}, confirm }) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (!modalProps.visible) {
      form.resetFields();
    }
  }, [modalProps.visible]);

  useEffect(() => {
    form.setFieldsValue({
      ...formData,
      buildDate: toDayjs(formData?.buildDate),
      buildTime: toDayjs(formData?.buildTime),
    });
  }, [formData, form]);

  const onFinish = (values) => {
    confirm &&
      confirm({
        ...values,
        buildDate: values?.buildDate ? dayjs(values.buildDate).format("YYYY-MM-DD") : undefined,
        buildTime: values?.buildTime
          ? dayjs(values.buildTime).format("YYYY-MM-DD HH:mm:ss")
          : undefined,
      });
  };

  return (
    <VtxModal {...modalProps} width={VtxModal.size.small} onOk={() => form.submit()} forceRender>
      <Form form={form} onFinish={onFinish}>
        <VtxFormLayout cols={2}>
          <VtxFormLayout.Row>
            <VtxFormLayout.FormItem label="编码" name="code" rules={[{ required: true, message: "必填" }]}>
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="名称" name="name" rules={[{ required: true, message: "必填" }]}>
              <VtxInput maxLength={128} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="类型" name="type">
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="日期类型" name="dateType">
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="建设日期" name="buildDate">
              <DatePicker allowClear style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="建设时间" name="buildTime">
              <DatePicker showTime allowClear style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="是否离线" name="hasOffline" valuePropName="checked">
              <Checkbox />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="管理人员ID" name="managerStaffId">
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="金额" name="amount">
              <InputNumber style={{ width: "100%" }} precision={2} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="版本" name="version">
              <InputNumber style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Row>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
