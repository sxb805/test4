/* eslint-disable react/prop-types */
import React from "react";
import { VtxFormLayout, VtxModal } from "@vtx/components";
import { Button, Form } from "antd";

const formatPersonTimes = (value) => {
  if (value === undefined || value === null || value === "") return "";
  const numberValue = Number(value);
  return Number.isNaN(numberValue) ? value : numberValue.toFixed(4).replace(/\.?0+$/, "");
};

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
          <VtxFormLayout.Card title="工作计划信息">
            <VtxFormLayout.FormItem label="项目编号">{formData.projectNo}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="项目名称">{formData.projectName}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="年份">{formData.year}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="一季度（人/次）">{formatPersonTimes(formData.firstQuarterPersonTimes)}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="二季度（人/次）">{formatPersonTimes(formData.secondQuarterPersonTimes)}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="三季度（人/次）">{formatPersonTimes(formData.thirdQuarterPersonTimes)}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="四季度（人/次）">{formatPersonTimes(formData.fourthQuarterPersonTimes)}</VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default View;
