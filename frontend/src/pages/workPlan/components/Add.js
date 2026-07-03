/* eslint-disable react/prop-types */
import React, { useEffect, useMemo, useState } from "react";
import { VtxFormLayout, VtxInput, VtxModal } from "@vtx/components";
import { DatePicker, Form, InputNumber, Select } from "antd";
import dayjs from "dayjs";
import { workPlanService } from "../service";

const toYearDayjs = (value) => {
  if (!value) return undefined;
  const parsed = dayjs(`${value}`, "YYYY");
  return parsed.isValid() ? parsed : undefined;
};

const toPersonTimesValue = (value) => {
  if (value === undefined || value === null || value === "") return undefined;
  const numberValue = Number(value);
  return Number.isNaN(numberValue) ? value : numberValue;
};

const personTimesRules = [
  {
    validator(_, value) {
      if (value === undefined || value === null || value === "") {
        return Promise.resolve();
      }
      if (Number(value) < 0) {
        return Promise.reject(new Error("必须大于等于0"));
      }
      if (!/^\d{1,7}(\.\d{1,4})?$/.test(`${value}`)) {
        return Promise.reject(new Error("最多7位整数、4位小数"));
      }
      return Promise.resolve();
    },
  },
];

const personTimesInputPattern = /^\d{0,7}(\.\d{0,4})?$/;
const controlKeys = ["Backspace", "Delete", "Tab", "Escape", "Enter", "ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", "Home", "End"];

const getNextInputValue = (input, text) => {
  const start = input.selectionStart ?? input.value.length;
  const end = input.selectionEnd ?? input.value.length;
  return `${input.value.slice(0, start)}${text}${input.value.slice(end)}`;
};

const personTimesInputProps = {
  style: { width: "100%" },
  min: 0,
  max: 9999999.9999,
  onKeyDown(event) {
    if (event.ctrlKey || event.metaKey || controlKeys.includes(event.key)) {
      return;
    }
    if (!/^\d|\.$/.test(event.key)) {
      event.preventDefault();
      return;
    }
    if (!personTimesInputPattern.test(getNextInputValue(event.currentTarget, event.key))) {
      event.preventDefault();
    }
  },
  onPaste(event) {
    const text = event.clipboardData.getData("text");
    if (!personTimesInputPattern.test(getNextInputValue(event.currentTarget, text))) {
      event.preventDefault();
    }
  },
};

function Add({ modalProps, formData = {}, confirm }) {
  const [form] = Form.useForm();
  const [projectOptions, setProjectOptions] = useState([]);

  useEffect(() => {
    if (!modalProps.visible) {
      form.resetFields();
    }
  }, [modalProps.visible, form]);

  useEffect(() => {
    form.setFieldsValue({
      ...formData,
      year: toYearDayjs(formData?.year),
      firstQuarterPersonTimes: toPersonTimesValue(formData?.firstQuarterPersonTimes),
      secondQuarterPersonTimes: toPersonTimesValue(formData?.secondQuarterPersonTimes),
      thirdQuarterPersonTimes: toPersonTimesValue(formData?.thirdQuarterPersonTimes),
      fourthQuarterPersonTimes: toPersonTimesValue(formData?.fourthQuarterPersonTimes),
    });
  }, [formData, form]);

  useEffect(() => {
    workPlanService
      .projectList()
      .then((res) => {
        const rows = Array.isArray(res?.data) ? res.data : [];
        setProjectOptions(
          rows.map((item) => ({
            label: `${item.name || ""}${item.code ? `(${item.code})` : ""}`,
            value: item.id,
            projectNo: item.code,
            projectName: item.name,
          })),
        );
      })
      .catch(() => setProjectOptions([]));
  }, []);

  const onFinish = (values) => {
    const project = projectOptions.find((item) => item.value === values.projectId);
    confirm &&
      confirm({
        ...values,
        projectNo: project?.projectNo,
        projectName: project?.projectName,
        year: values.year ? Number(dayjs(values.year).format("YYYY")) : undefined,
      });
  };

  const onProjectChange = (projectId) => {
    const project = projectOptions.find((item) => item.value === projectId);
    form.setFieldsValue({
      projectNo: project?.projectNo,
    });
  };

  const projectSelectOptions = useMemo(() => projectOptions, [projectOptions]);

  return (
    <VtxModal {...modalProps} width={VtxModal.size.large} onOk={() => form.submit()} forceRender>
      <Form form={form} onFinish={onFinish}>
        <VtxFormLayout cols={2}>
            <VtxFormLayout.Card title="工作计划信息">
            <VtxFormLayout.FormItem label="项目名称" name="projectId" rules={[{ required: true, message: "必填" }]}>
              <Select
                showSearch
                allowClear
                placeholder="请选择项目"
                options={projectSelectOptions}
                optionFilterProp="label"
                onChange={onProjectChange}
              />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="项目编号" name="projectNo">
              <VtxInput disabled />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="年份" name="year" rules={[{ required: true, message: "必填" }]}>
              <DatePicker picker="year" style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="一季度（人/次）" name="firstQuarterPersonTimes" rules={personTimesRules}>
              <InputNumber {...personTimesInputProps} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="二季度（人/次）" name="secondQuarterPersonTimes" rules={personTimesRules}>
              <InputNumber {...personTimesInputProps} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="三季度（人/次）" name="thirdQuarterPersonTimes" rules={personTimesRules}>
              <InputNumber {...personTimesInputProps} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="四季度（人/次）" name="fourthQuarterPersonTimes" rules={personTimesRules}>
              <InputNumber {...personTimesInputProps} />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
