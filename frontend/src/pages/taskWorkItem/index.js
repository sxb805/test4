import React from "react";
import StatusTag from "@/components/status-tag";
import useFormModal from "@/hooks/useFormModal";
import useNameSpace from "@/hooks/useNameSpace";
import usePermission from "@/hooks/usePermission";
import useTable from "@/hooks/useTable";
import { ImportOutlined, PlusOutlined } from "@ant-design/icons";
import { VtxDatagrid, VtxImport2, VtxPageLayout, VtxSearch } from "@vtx/components";
import { Button, DatePicker, Form, Select, TreeSelect, message } from "antd";
import dayjs from "dayjs";
import { default as Add, default as Edit } from "./components/Add";
import Export from "./components/Export";
import View from "./components/View";
import { API_PREFIX, taskWorkItemService } from "./service";

const { TableLayout, ButtonWrap } = VtxPageLayout;
const { RangePicker } = DatePicker;

const STATUS_OPTIONS = [
  { label: "完成", value: "完成" },
  { label: "延期", value: "延期" },
];
const STATUS_COLOR_MAP = {
  完成: "#52c41a",
  延期: "#ff4d4f",
};
const PROJECT_TYPE_OPTIONS = [
  { label: "项目", value: "PROJECT" },
  { label: "产品", value: "PRODUCT" },
];

const getPageParam = (name) => {
  const searchValue = new URLSearchParams(window.location.search || "").get(name);
  if (searchValue) {
    return searchValue;
  }
  const hash = window.location.hash || "";
  const queryIndex = hash.indexOf("?");
  return queryIndex >= 0 ? new URLSearchParams(hash.slice(queryIndex + 1)).get(name) || "" : "";
};

function TaskWorkItem() {
  const { act } = useNameSpace("taskWorkItem");
  const { validate } = usePermission(["add", "edit", "delete", "import", "export"]);
  const [staffTree, setStaffTree] = React.useState([]);
  const [projectOptions, setProjectOptions] = React.useState([]);
  const boundProjectType = React.useMemo(() => {
    const projectType = getPageParam("projectType");
    return PROJECT_TYPE_OPTIONS.some((item) => item.value === projectType) ? projectType : "";
  }, []);

  const authParams = React.useMemo(
    () => ({
      tenantId: getPageParam("tenantId") || sessionStorage.getItem("tenantId") || "",
      userId: getPageParam("userId") || sessionStorage.getItem("userId") || "",
    }),
    [],
  );

  const normalizeTree = React.useCallback((nodes = []) => {
    return (Array.isArray(nodes) ? nodes : []).map((node) => {
      const rawValue = node?.attributes?.id || node?.key;
      const rawLabel = node?.name || rawValue;
      const isStaff = node?.type === "Staff";
      return {
        ...node,
        value: rawValue,
        key: node?.key || rawValue,
        title: rawLabel,
        label: rawLabel,
        selectable: isStaff,
        disabled: !isStaff,
        children: normalizeTree(node?.children || node?.child || node?.nodes || []),
      };
    });
  }, []);

  React.useEffect(() => {
    taskWorkItemService
      .loadStaffTree()
      .then((res) => {
        const root = res?.data;
        setStaffTree(root ? normalizeTree([root]) : []);
      })
      .catch(() => {
        setStaffTree([]);
      });

    taskWorkItemService
      .projectList(boundProjectType ? { type: boundProjectType } : {})
      .then((res) => {
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
      })
      .catch(() => setProjectOptions([]));
  }, [boundProjectType, normalizeTree]);

  const commonColumnParam = [
    ["项目编号", "projectNo"],
    ["项目名称", "projectName"],
    ["项目类型", "projectTypeName"],
    ["所属TL", "ownerTlName"],
    ["模块", "moduleName"],
    ["任务描述", "taskDesc"],
    [
      "开始日期",
      "startDate",
      {
        render(text) {
          return text ? dayjs(text).format("YYYY-MM-DD") : "";
        },
      },
    ],
    [
      "结束日期",
      "endDate",
      {
        render(text) {
          return text ? dayjs(text).format("YYYY-MM-DD") : "";
        },
      },
    ],
    [
      "预计工时(h)",
      "estimatedHours",
      {
        align: "right",
      },
    ],
    ["责任人", "ownerName"],
    [
      "完成状态",
      "status",
      {
        render(text) {
          if (!text) return "";
          return <StatusTag text={text} color={STATUS_COLOR_MAP[text] || "#d9d9d9"} />;
        },
      },
    ],
    [
      "实际完成日期",
      "actualFinishDate",
      {
        render(text) {
          return text ? dayjs(text).format("YYYY-MM-DD") : "";
        },
      },
    ],
    [
      "实际工时(h)",
      "actualHours",
      {
        align: "right",
      },
    ],
    ["实际完成人", "actualOwnerName"],
  ];

  const columnParam = [
    ...commonColumnParam,
    [
      "操作",
      "action",
      {
        width: 180,
        renderButtons(text, record) {
          return [
            {
              name: "查看",
              onClick() {
                openView(record);
              },
            },
            {
              name: "编辑",
              onClick() {
                openEdit(record);
              },
              visible: validate("edit"),
            },
            {
              name: "删除",
              popconfirm: {
                title: "确认删除吗?",
                okButtonProps: { danger: true },
                confirm() {
                  deleteRows.run([record.id]);
                },
              },
              visible: validate("delete"),
            },
          ];
        },
      },
    ],
  ];

  const {
    form,
    deleteRows,
    datagridProps,
    submit,
    reset,
    selectedRowKeys,
    setImportVisible,
    importVisible,
    importProps,
  } = useTable({
    act,
    columnParam,
    dealFormData(formData) {
      const range = formData?.startDateRange || [];
      return {
        projectId: formData?.projectId,
        projectType: boundProjectType || undefined,
        ownerTlId: formData?.ownerTlId,
        status: formData?.status,
        startDateBegin: range?.[0] ? dayjs(range[0]).format("YYYY-MM-DD") : undefined,
        startDateEnd: range?.[1] ? dayjs(range[1]).format("YYYY-MM-DD") : undefined,
      };
    },
    importURL: `${API_PREFIX}/taskWorkItem/importExcel?${new URLSearchParams({
      tenantId: authParams.tenantId,
      userId: authParams.userId,
    }).toString()}`,
    importTemplateURL: "./resources/template/任务工时管理导入模板.xlsx",
    errorURL: `${API_PREFIX}/common/downloadImportExcel`,
    importProp: {
      title: "任务工时管理",
      modalWidth: 1200,
      afterUpload: (payload) => {
        if (!payload) {
          message.error("导入失败");
          return;
        }
        let data = payload;
        if (typeof payload === "string") {
          try {
            data = JSON.parse(payload);
          } catch {
            message.error("导入失败");
            return;
          }
        }
        if (data?.result === 0) {
          message.success("导入成功");
          setImportVisible(false);
          datagridProps.onRefresh?.();
          return;
        }
        message.error(data?.msg || "导入失败");
        datagridProps.onRefresh?.();
      },
    },
  });

  const addFormModal = useFormModal({
    modal: { title: "新增" },
    service: (params) =>
      act("saveOrUpdate", { params, type: "save" }).then((res) => {
        if (res?.success) {
          datagridProps.onRefresh?.();
          message.success("新增成功");
          addFormModal.setVisible(false);
        } else {
          message.error(res?.msg || "操作失败");
        }
        return res?.success;
      }),
  });

  const editFormModal = useFormModal({
    modal: { title: "编辑" },
    service: (params) =>
      act("saveOrUpdate", { params, type: "update", id: editFormModal.formData?.id }).then((res) => {
        if (res?.success) {
          datagridProps.onRefresh?.();
          message.success("编辑成功");
          editFormModal.setVisible(false);
        } else {
          message.error(res?.msg || "操作失败");
        }
        return res?.success;
      }),
  });

  const viewFormModal = useFormModal({ modal: { title: "查看" } });

  const openView = (record) => {
    viewFormModal.setFormData(record || {});
    viewFormModal.setVisible(true);
    act("view", { id: record.id }).then((data) => {
      if (data && Object.keys(data).length > 0) {
        viewFormModal.setFormData({ ...record, ...data });
      }
    });
  };

  const openEdit = (record) => {
    editFormModal.setFormData(record || {});
    editFormModal.setVisible(true);
    act("view", { id: record.id }).then((data) => {
      if (data && Object.keys(data).length > 0) {
        editFormModal.setFormData({ ...record, ...data });
      }
    });
  };

  const exportColumns = commonColumnParam.map((item) => ({ title: item[0], key: item[1], field: item[1] }));

  const defaultDateRange = React.useMemo(() => [dayjs().startOf("year"), dayjs()], []);

  return (
    <TableLayout.Page>
      <TableLayout.Search>
        <Form form={form} name="query-form" initialValues={{ startDateRange: defaultDateRange }}>
          <VtxSearch titles={["项目名称", "所属TL", "开始日期", "完成状态"]} gridWeight={[1, 1, 1, 1]} onConfirm={submit} onClear={reset}>
            <Form.Item name="projectId">
              <Select showSearch allowClear placeholder="请选择项目" options={projectOptions} optionFilterProp="label" />
            </Form.Item>
            <Form.Item name="ownerTlId">
              <TreeSelect allowClear showSearch treeNodeFilterProp="title" treeData={staffTree} placeholder="请选择所属TL" treeDefaultExpandAll />
            </Form.Item>
            <Form.Item name="startDateRange">
              <RangePicker style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item name="status">
              <Select allowClear options={STATUS_OPTIONS} placeholder="请选择完成状态" />
            </Form.Item>
          </VtxSearch>
        </Form>
      </TableLayout.Search>
      <TableLayout.Content>
        <TableLayout.Table>
          <VtxDatagrid
            {...datagridProps}
            buttonGroup={
              <ButtonWrap>
                {validate("add") && (
                  <Button icon={<PlusOutlined />} type="primary" onClick={() => addFormModal.setVisible(true)}>
                    新增
                  </Button>
                )}
                {validate("import") && (
                  <Button onClick={() => setImportVisible(true)} icon={<ImportOutlined />}>
                    导入
                  </Button>
                )}
                {validate("export") && (
                  <Export
                    selectedRowKeys={selectedRowKeys}
                    tableData={datagridProps.dataSource || []}
                    params={{
                      ...form.getFieldsValue(),
                      projectType: boundProjectType || undefined,
                      startDateBegin: form.getFieldValue("startDateRange")?.[0]
                        ? dayjs(form.getFieldValue("startDateRange")[0]).format("YYYY-MM-DD")
                        : undefined,
                      startDateEnd: form.getFieldValue("startDateRange")?.[1]
                        ? dayjs(form.getFieldValue("startDateRange")[1]).format("YYYY-MM-DD")
                        : undefined,
                    }}
                    requestExportUrl={`${API_PREFIX}/taskWorkItem/exportExcel`}
                    columns={exportColumns}
                    fileName="任务工时管理导出"
                  />
                )}
              </ButtonWrap>
            }
          />
        </TableLayout.Table>
      </TableLayout.Content>
      <Add {...addFormModal} projectType={boundProjectType} />
      <Edit {...editFormModal} projectType={boundProjectType} />
      <View {...viewFormModal} />
      {importVisible && <VtxImport2 {...importProps} />}
    </TableLayout.Page>
  );
}

export default TaskWorkItem;
