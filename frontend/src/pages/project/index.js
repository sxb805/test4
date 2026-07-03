import React from "react";
import useFormModal from "@/hooks/useFormModal";
import useNameSpace from "@/hooks/useNameSpace";
import usePermission from "@/hooks/usePermission";
import useTable from "@/hooks/useTable";
import { ImportOutlined, PlusOutlined } from "@ant-design/icons";
import { VtxDatagrid, VtxImport2, VtxInput, VtxPageLayout, VtxSearch } from "@vtx/components";
import { Button, Form, Select, TreeSelect, message } from "antd";
import { default as Add, default as Edit } from "./components/Add";
import Export from "./components/Export";
import View from "./components/View";
import { API_PREFIX, projectService } from "./service";

const { TableLayout, ButtonWrap } = VtxPageLayout;
const PROJECT_TYPE_OPTIONS = [
  { label: "项目", value: "PROJECT" },
  { label: "产品", value: "PRODUCT" },
];

function Project() {
  const { act } = useNameSpace("project");
  const { validate } = usePermission(["add", "edit", "delete", "import", "export"]);
  const [staffTree, setStaffTree] = React.useState([]);
  const authParams = React.useMemo(
    () => ({
      tenantId: new URLSearchParams(window.location.search).get("tenantId") || sessionStorage.getItem("tenantId") || "",
      userId: new URLSearchParams(window.location.search).get("userId") || sessionStorage.getItem("userId") || "",
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
    projectService
      .loadStaffTree()
      .then((res) => {
        const root = res?.data;
        setStaffTree(root ? normalizeTree([root]) : []);
      })
      .catch(() => {
        setStaffTree([]);
      });
  }, [authParams, normalizeTree]);

  const commonColumnParam = [["编号", "code"], ["名称", "name"], ["类型", "typeName"], ["TL", "tlName"]];

  const columnParam = [
    ...commonColumnParam,
    [
      "操作",
      "action",
      {
        width: 180,
        renderButtons(text, record) {
          return [
            { name: "查看", onClick() { openView(record); } },
            { name: "编辑", onClick() { openEdit(record); }, visible: validate("edit") },
            {
              name: "删除",
              popconfirm: {
                title: "确认删除吗?",
                okButtonProps: { danger: true },
                confirm() { deleteRows.run([record.id]); },
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
    importURL: `${API_PREFIX}/project/importExcel?${new URLSearchParams({
      tenantId: authParams.tenantId,
      userId: authParams.userId,
    }).toString()}`,
    importTemplateURL: "./resources/template/项目导入模板.xlsx",
    errorURL: `${API_PREFIX}/common/downloadImportExcel`,
    importProp: {
      title: "项目",
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
    service: (params) => act("saveOrUpdate", { params, type: "save" }).then((res) => {
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
    service: (params) => act("saveOrUpdate", { params, type: "update", id: editFormModal.formData?.id }).then((res) => {
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

  return (
    <TableLayout.Page>
      <TableLayout.Search>
        <Form form={form} name="query-form">
          <VtxSearch titles={["编号", "名称", "类型", "TL"]} gridWeight={[1, 1, 1, 1]} onConfirm={submit} onClear={reset}>
            <Form.Item name="code">
              <VtxInput maxLength={32} />
            </Form.Item>
            <Form.Item name="name">
              <VtxInput maxLength={100} />
            </Form.Item>
            <Form.Item name="type">
              <Select allowClear options={PROJECT_TYPE_OPTIONS} placeholder="请选择类型" />
            </Form.Item>
            <Form.Item name="tlId">
              <TreeSelect
                allowClear
                showSearch
                treeNodeFilterProp="title"
                treeData={staffTree}
                placeholder="请选择TL"
                treeDefaultExpandAll
              />
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
                    params={form.getFieldsValue()}
                    requestExportUrl={`${API_PREFIX}/project/exportExcel`}
                    columns={exportColumns}
                    fileName="项目导出"
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

export default Project;
