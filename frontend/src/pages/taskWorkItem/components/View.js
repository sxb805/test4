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
      width={VtxModal.size.large}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          关闭
        </Button>,
      ]}
    >
      <Form layout="inline">
        <VtxFormLayout mode="view">
          <VtxFormLayout.Card title="任务计划信息">
            <VtxFormLayout.FormItem label="项目名称">{formData.projectName}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="项目编号">{formData.projectNo}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="所属TL">{formData.ownerTlName}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="模块">{formData.moduleName}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="责任人">{formData.ownerName}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="开始日期">{formData.startDate ? dayjs(formData.startDate).format("YYYY-MM-DD") : ""}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="结束日期">{formData.endDate ? dayjs(formData.endDate).format("YYYY-MM-DD") : ""}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="预计工时(h)">{formData.estimatedHours}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="完成状态">{formData.status}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="任务描述" weights={2}>{formData.taskDesc}</VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
          <VtxFormLayout.Card title="任务执行与结果">
            <VtxFormLayout.FormItem label="实际完成日期">{formData.actualFinishDate ? dayjs(formData.actualFinishDate).format("YYYY-MM-DD") : ""}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="实际工时(h)">{formData.actualHours}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="实际完成人">{formData.actualOwnerName}</VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="任务进度跟进描述" weights={2}>{formData.progressNote}</VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default View;
