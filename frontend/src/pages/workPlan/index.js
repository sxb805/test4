import React from "react";
import useFormModal from "@/hooks/useFormModal";
import useNameSpace from "@/hooks/useNameSpace";
import usePermission from "@/hooks/usePermission";
import useTable from "@/hooks/useTable";
import { ImportOutlined, PlusOutlined } from "@ant-design/icons";
import { VtxDatagrid, VtxImport2, VtxInput, VtxPageLayout, VtxSearch } from "@vtx/components";
import { Button, DatePicker, Form, message } from "antd";
import dayjs from "dayjs";
import { default as Add, default as Edit } from "./components/Add";
import Export from "./components/Export";
import View from "./components/View";
import { API_PREFIX } from "./service";

const { TableLayout, ButtonWrap } = VtxPageLayout;

const formatPersonTimes = (value) => {
  if (value === undefined || value === null || value === "") return "";
  const numberValue = Number(value);
  return Number.isNaN(numberValue) ? value : numberValue.toFixed(3).replace(/\.?0+$/, "");
};

function WorkPlan() {
  const { act } = useNameSpace("workPlan");
  const { validate } = usePermission(["add", "edit", "delete", "import", "export"]);
  const authParams = React.useMemo(
    () => ({
      tenantId: new URLSearchParams(window.location.search).get("tenantId") || sessionStorage.getItem("tenantId") || "",
      userId: new URLSearchParams(window.location.search).get("userId") || sessionStorage.getItem("userId") || "",
    }),
    [],
  );

  const commonColumnParam = [
    ["项目编号", "projectNo"],
    ["项目名称", "projectName"],
    ["年份", "year"],
    [
      "一季度（人/次）",
      "firstQuarterPersonTimes",
      {
        align: "right",
        render: formatPersonTimes,
      },
    ],
    [
      "二季度（人/次）",
      "secondQuarterPersonTimes",
      {
        align: "right",
        render: formatPersonTimes,
      },
    ],
    [
      "三季度（人/次）",
      "thirdQuarterPersonTimes",
      {
        align: "right",
        render: formatPersonTimes,
      },
    ],
    [
      "四季度（人/次）",
      "fourthQuarterPersonTimes",
      {
        align: "right",
        render: formatPersonTimes,
      },
    ],
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
      return {
        projectNo: formData?.projectNo,
        projectName: formData?.projectName,
        year: formData?.year ? Number(dayjs(formData.year).format("YYYY")) : undefined,
      };
    },
    importURL: `${API_PREFIX}/workPlan/importExcel?${new URLSearchParams({
      tenantId: authParams.tenantId,
      userId: authParams.userId,
    }).toString()}`,
    importTemplateURL: "./resources/template/工作计划管理导入模板.xlsx",
    errorURL: `${API_PREFIX}/common/downloadImportExcel`,
    importProp: {
      title: "工作计划管理",
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
  const getSearchParams = () => {
    const values = form.getFieldsValue();
    return {
      projectNo: values?.projectNo,
      projectName: values?.projectName,
      year: values?.year ? Number(dayjs(values.year).format("YYYY")) : undefined,
    };
  };

  return (
    <TableLayout.Page>
      <TableLayout.Search>
        <Form form={form} name="query-form">
          <VtxSearch titles={["项目编号", "项目名称", "年份"]} gridWeight={[1, 1, 1]} onConfirm={submit} onClear={reset}>
            <Form.Item name="projectNo">
              <VtxInput maxLength={64} />
            </Form.Item>
            <Form.Item name="projectName">
              <VtxInput maxLength={200} />
            </Form.Item>
            <Form.Item name="year">
              <DatePicker picker="year" style={{ width: "100%" }} />
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
                    params={getSearchParams()}
                    requestExportUrl={`${API_PREFIX}/workPlan/exportExcel`}
                    columns={exportColumns}
                    fileName="工作计划管理导出"
                  />
                )}
              </ButtonWrap>
            }
          />
        </TableLayout.Table>
      </TableLayout.Content>
      <Add {...addFormModal} />
      <Edit {...editFormModal} />
      <View {...viewFormModal} />
      {importVisible && <VtxImport2 {...importProps} />}
    </TableLayout.Page>
  );
}

export default WorkPlan;
