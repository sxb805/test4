/* eslint-disable react/prop-types */
import React from "react";
import { VtxFormLayout, VtxModal, VtxUpload } from "@vtx/components";
import { Button, Form } from "antd";
import dayjs from "dayjs";

const parsePictureFile = (files) => {
  if (!files) return [];
  try {
    const parsed = JSON.parse(files);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const getLocationInfo = (location) => {
  const lngLats = location?.lngLats || "";
  const [longitude, latitude] = lngLats.split(",");
  return {
    longitude,
    latitude,
  };
};

function View(props) {
  const { modalProps, formData = {} } = props;
  const { onCancel } = modalProps;
  const locationInfo = getLocationInfo(formData?.location);

  return (
    <VtxModal
      {...modalProps}
      width={VtxModal.size.large}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          关闭
        </Button>,
      ]}
    >
      <Form layout="inline">
        <VtxFormLayout mode="view">
          <VtxFormLayout.Card title="样例信息">
            <VtxFormLayout.FormItem label="编码">{formData.code}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="名称">{formData.name}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="类型">{formData.typeName || formData.type}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="日期类型">
              {formData.dateTypeName || formData.dateType}
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="建设日期">
              {formData.buildDate ? dayjs(formData.buildDate).format("YYYY-MM-DD") : ""}
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="建设时间">
              {formData.buildTime ? dayjs(formData.buildTime).format("YYYY-MM-DD HH:mm:ss") : ""}
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="是否离线">{formData.hasOffline ? "是" : "否"}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="管理人员">
              {formData.managerStaffName || formData.managerStaffId}
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="金额">{formData.amount}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="版本">{formData.version}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="地址" weights={2}>{formData.address}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="经度">{locationInfo.longitude}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="纬度">{locationInfo.latitude}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="照片" weights={2}>
              <VtxUpload viewMode listType="picture-card" fileList={parsePictureFile(formData?.files)} />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default View;
