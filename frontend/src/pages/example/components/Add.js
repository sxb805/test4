/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { VtxFormLayout, VtxInput, VtxModal, VtxUpload } from "@vtx/components";
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
  const [fileList, setFileList] = useState([]);

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
    if (formData?.files) {
      try {
        const parsed = JSON.parse(formData.files);
        setFileList(Array.isArray(parsed) ? parsed : []);
      } catch {
        setFileList([]);
      }
    } else {
      setFileList([]);
    }
  }, [formData, form]);

  const onFinish = (values) => {
    confirm &&
      confirm({
        ...values,
        files: fileList?.length ? JSON.stringify(fileList) : "",
        buildDate: values?.buildDate ? dayjs(values.buildDate).format("YYYY-MM-DD") : undefined,
        buildTime: values?.buildTime
          ? dayjs(values.buildTime).format("YYYY-MM-DD HH:mm:ss")
          : undefined,
      });
  };

  return (
    <VtxModal {...modalProps} width={VtxModal.size.large} onOk={() => form.submit()} forceRender>
      <Form form={form} onFinish={onFinish}>
        <VtxFormLayout cols={2}>
          <VtxFormLayout.Card title="样例信息">
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
            <VtxFormLayout.FormItem
              label="管理人员ID"
              name="managerStaffId"
              extra="请填写人员ID；导入模板请填写管理人员姓名。"
            >
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="金额" name="amount">
              <InputNumber style={{ width: "100%" }} precision={2} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="版本" name="version">
              <InputNumber style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="照片" name="files" weights={2} tooltip="上限3张，单个照片小于5M">
              <VtxUpload
                maxNum={3}
                listType="picture-card"
                fileList={fileList}
                onSuccess={(file) => {
                  setFileList([...fileList, { id: file.id, name: file.name, type: file.type }]);
                }}
                onRemove={(file) => {
                  setFileList(fileList.filter((item) => item.id !== file.id));
                }}
                flag={new Date()}
                accept="image/*"
              />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
