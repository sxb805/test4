/* eslint-disable react/prop-types */
import React, { useEffect } from "react";
import { VtxFormLayout, VtxInput, VtxModal } from "@vtx/components";
import { Form, Input } from "antd";

function Add({ modalProps, formData = {}, confirm }) {
  const [form] = Form.useForm();
  const isEdit = Boolean(formData?.id);

  useEffect(() => {
    if (!modalProps.visible) {
      form.resetFields();
    }
  }, [modalProps.visible]);

  useEffect(() => {
    form.setFieldsValue(formData);
  }, [formData, form]);

  const onFinish = (values) => {
    confirm && confirm(values);
  };

  return (
    <VtxModal
      {...modalProps}
      width={VtxModal.size.small}
      onOk={() => form.submit()}
      forceRender
    >
      <Form form={form} onFinish={onFinish} initialValues={formData}>
        <VtxFormLayout cols={1}>
          <VtxFormLayout.Row>
            <VtxFormLayout.FormItem
              label="登录账号"
              name="username"
              rules={[{ required: true, message: "必填" }]}
            >
              <VtxInput />
            </VtxFormLayout.FormItem>
            {!isEdit && (
              <>
                <VtxFormLayout.FormItem
                  label="登录密码"
                  name="password"
                  rules={[{ required: true, message: "必填" }]}
                >
                  <Input.Password />
                </VtxFormLayout.FormItem>
                <VtxFormLayout.FormItem
                  label="确认密码"
                  name="confirmPassword"
                  dependencies={["password"]}
                  rules={[
                    { required: true, message: "必填" },
                    ({ getFieldValue }) => ({
                      validator(_, value) {
                        if (!value || getFieldValue("password") === value) {
                          return Promise.resolve();
                        }
                        return Promise.reject(new Error("两次输入的密码不一致"));
                      },
                    }),
                  ]}
                >
                  <Input.Password />
                </VtxFormLayout.FormItem>
              </>
            )}
            <VtxFormLayout.FormItem
              label="用户姓名"
              name="realName"
              rules={[{ required: true, message: "必填" }]}
            >
              <VtxInput />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Row>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
