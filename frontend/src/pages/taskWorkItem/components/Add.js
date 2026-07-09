/* eslint-disable react/prop-types */
import React, { useEffect, useMemo, useState } from "react";
import { VtxFormLayout, VtxInput, VtxModal } from "@vtx/components";
import { DatePicker, Form, Input, InputNumber, Select, TreeSelect } from "antd";
import dayjs from "dayjs";
import { taskWorkItemService } from "../service";

const STATUS_OPTIONS = [
  { label: "完成", value: "完成" },
  { label: "延期", value: "延期" },
];
const PROJECT_TYPE_LABEL_MAP = {
  PROJECT: "项目",
  PRODUCT: "产品",
};
const COMPANY_OPTIONS = [
  { label: "苏州伏泰", value: "SUZHOU_FUTAI" },
  { label: "苏州环境云", value: "SUZHOU_ENV_CLOUD" },
];

const toDayjs = (value) => {
  if (!value) return undefined;
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed : undefined;
};

function Add({ modalProps, formData = {}, confirm, projectType, company }) {
  const [form] = Form.useForm();
  const [projectOptions, setProjectOptions] = useState([]);
  const [staffTree, setStaffTree] = useState([]);
  const [staffNameMap, setStaffNameMap] = useState({});

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
  }, [modalProps.visible, form]);

  useEffect(() => {
    form.setFieldsValue({
      ...formData,
      company: company || formData?.company,
      projectTypeName: formData?.projectTypeName || PROJECT_TYPE_LABEL_MAP[formData?.projectType],
      startDate: toDayjs(formData?.startDate),
      endDate: toDayjs(formData?.endDate),
      actualFinishDate: toDayjs(formData?.actualFinishDate),
    });
  }, [company, formData, form]);

  useEffect(() => {
    taskWorkItemService.projectList(projectType ? { type: projectType } : {}).then((res) => {
      const rows = Array.isArray(res?.data) ? res.data : [];
      setProjectOptions(
        rows.map((item) => ({
          label: `${item.name || ""}${item.code ? `(${item.code})` : ""}`,
          value: item.id,
          projectNo: item.code,
          projectName: item.name,
          projectType: item.type,
          projectTypeName: item.typeName,
        })),
      );
    }).catch(() => setProjectOptions([]));

    taskWorkItemService
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
  }, [projectType]);

  const handleProjectChange = (value) => {
    const selectedProject = projectOptions.find((item) => item.value === value);
    form.setFieldsValue({
      projectNo: selectedProject?.projectNo,
      projectType: selectedProject?.projectType,
      projectTypeName: selectedProject?.projectTypeName,
    });
  };

  const onFinish = (values) => {
    const selectedProject = projectOptions.find((item) => item.value === values.projectId);
    confirm &&
      confirm({
        ...values,
        projectNo: selectedProject?.projectNo,
        projectName: selectedProject?.projectName,
        projectType: selectedProject?.projectType,
        projectTypeName: selectedProject?.projectTypeName,
        company: company || values.company,
        ownerTlName: staffNameMap[values.ownerTlId],
        ownerName: staffNameMap[values.ownerId],
        actualOwnerName: values.actualOwnerId ? staffNameMap[values.actualOwnerId] : undefined,
        startDate: values.startDate ? dayjs(values.startDate).format("YYYY-MM-DD") : undefined,
        endDate: values.endDate ? dayjs(values.endDate).format("YYYY-MM-DD") : undefined,
        actualFinishDate: values.actualFinishDate ? dayjs(values.actualFinishDate).format("YYYY-MM-DD") : undefined,
      });
  };

  const projectSelectOptions = useMemo(() => projectOptions, [projectOptions]);

  return (
    <VtxModal {...modalProps} width={VtxModal.size.large} onOk={() => form.submit()} forceRender>
      <Form form={form} onFinish={onFinish}>
        <VtxFormLayout cols={2}>
          <VtxFormLayout.Card title="任务计划信息">
            <VtxFormLayout.FormItem label="项目名称" name="projectId" rules={[{ required: true, message: "必填" }]}>
              <Select
                showSearch
                allowClear
                placeholder="请选择项目"
                options={projectSelectOptions}
                optionFilterProp="label"
                onChange={handleProjectChange}
              />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="项目编号" name="projectNo">
              <VtxInput disabled />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="项目类型" name="projectTypeName">
              <VtxInput disabled />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="所属公司" name="company" rules={[{ required: true, message: "必填" }]}>
              <Select
                allowClear={!company}
                disabled={Boolean(company)}
                options={COMPANY_OPTIONS}
                placeholder="请选择所属公司"
              />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="所属TL" name="ownerTlId" rules={[{ required: true, message: "必填" }]}>
              <TreeSelect allowClear showSearch treeNodeFilterProp="title" treeData={staffTree} placeholder="请选择所属TL" treeDefaultExpandAll />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="模块" name="moduleName" rules={[{ max: 100, message: "最大100字符" }]}>
              <VtxInput maxLength={100} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="责任人" name="ownerId" rules={[{ required: true, message: "必填" }]}>
              <TreeSelect allowClear showSearch treeNodeFilterProp="title" treeData={staffTree} placeholder="请选择责任人" treeDefaultExpandAll />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="开始日期" name="startDate" rules={[{ required: true, message: "必填" }]}>
              <DatePicker style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="结束日期" name="endDate" rules={[{ required: true, message: "必填" }]}>
              <DatePicker style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="预计工时(h)" name="estimatedHours">
              <InputNumber style={{ width: "100%" }} min={0} precision={0} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="完成状态" name="status" rules={[{ required: true, message: "必填" }]}>
              <Select allowClear options={STATUS_OPTIONS} placeholder="请选择完成状态" />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="任务描述" name="taskDesc" rules={[{ required: true, message: "必填" }, { max: 2000, message: "最大2000字符" }]} weights={2}>
              <Input.TextArea maxLength={2000} rows={3} />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
          <VtxFormLayout.Card title="任务执行与结果">
            <VtxFormLayout.FormItem label="实际完成日期" name="actualFinishDate">
              <DatePicker style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="实际工时(h)" name="actualHours">
              <InputNumber style={{ width: "100%" }} min={0} precision={0} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="实际完成人" name="actualOwnerId">
              <TreeSelect allowClear showSearch treeNodeFilterProp="title" treeData={staffTree} placeholder="请选择实际完成人" treeDefaultExpandAll />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="任务进度跟进描述" name="progressNote" rules={[{ max: 2000, message: "最大2000字符" }]} weights={2}>
              <Input.TextArea maxLength={2000} rows={3} />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
