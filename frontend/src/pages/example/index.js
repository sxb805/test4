/**
 * permissions:
 *  add: 新增
 *  edit: 编辑
 *  export: 导出
 *  delete: 删除
 *  import: 导入
 */
import React from "react";

import useFormModal from "@/hooks/useFormModal";
import useNameSpace from "@/hooks/useNameSpace";
import usePermission from "@/hooks/usePermission";
import useTable from "@/hooks/useTable";
import { ImportOutlined, PlusOutlined } from "@ant-design/icons";
import {
  VtxDatagrid,
  VtxImport2,
  VtxInput,
  VtxPageLayout,
  VtxSearch,
  useIntl,
} from "@vtx/components";
import dayjs from "dayjs";
import { Button, DatePicker, Form, message } from "antd";
import { TreeSelect } from "antd";
import { default as Add, default as Edit } from "./components/Add";
import Export from "./components/Export";
import View from "./components/View";
import { exampleService } from "./service";

const { TableLayout, ButtonWrap } = VtxPageLayout;

function Example() {
  const intl = useIntl();
  const { act } = useNameSpace("example");
  const [staffTree, setStaffTree] = React.useState([]);

  const { validate } = usePermission(["add", "edit", "delete", "import", "export"]);

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
    exampleService
      .loadStaffTree()
      .then((res) => {
        const root = res?.data;
        setStaffTree(root ? normalizeTree([root]) : []);
      })
      .catch(() => {
        setStaffTree([]);
      });
  }, [normalizeTree]);

  const commonColumnParam = [
    ["编码", "code"],
    ["名称", "name"],
    ["类型", "typeName"],
    [
      "建设日期",
      "buildDate",
      {
        render(text) {
          return text ? dayjs(text).format("YYYY-MM-DD") : "";
        },
      },
    ],
    [
      "建设时间",
      "buildTime",
      {
        render(text) {
          return text ? dayjs(text).format("YYYY-MM-DD HH:mm:ss") : "";
        },
      },
    ],
    [
      "是否离线",
      "hasOffline",
      {
        render(text) {
          return text ? "是" : "否";
        },
      },
    ],
    ["金额", "amount"],
    ["版本", "version"],
    ["管理人员", "managerStaffName"],
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
              name: intl.getMessage("datagrid.view", "查看"),
              onClick() {
                openView(record);
              },
            },
            {
              name: intl.getMessage("datagrid.update", "编辑"),
              onClick() {
                openEdit(record);
              },
              visible: validate("edit"),
            },
            {
              name: intl.getMessage("datagrid.delete", "删除"),
              popconfirm: {
                title: `${intl.getMessage(
                  "editableTable.action.deleteConfirm",
                  "确认删除吗",
                )}?`,
                okButtonProps: {
                  danger: true,
                },
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
        ...formData,
        buildDate: formData?.buildDate ? dayjs(formData.buildDate).format("YYYY-MM-DD") : undefined,
      };
    },
    importURL: `/cloud/sample/example/importExcel?${new URLSearchParams({
      tenantId:
        new URLSearchParams(window.location.search).get("tenantId") ||
        sessionStorage.getItem("tenantId") ||
        "",
      userId:
        new URLSearchParams(window.location.search).get("userId") ||
        sessionStorage.getItem("userId") ||
        "",
    }).toString()}`,
    importTemplateURL: "/resources/template/样例导入模板.xlsx",
    errorURL: "/cloud/sample/common/downloadImportExcel",
    importProp: {
      title: "样例",
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
        if (data?.result === 0 && (!data.data || data.data.length === 0)) {
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
    modal: {
      title: intl.getMessage("datagrid.add", "新增"),
    },
    service: (params) => {
      return act("saveOrUpdate", {
        params,
        type: "save",
      }).then((res) => {
        if (res?.success) {
          handleRefresh();
          message.success("新增成功");
          addFormModal.setVisible(false);
        } else {
          message.error(res?.msg || "新增失败");
        }
        return res?.success;
      });
    },
  });

  const editFormModal = useFormModal({
    modal: {
      title: intl.getMessage("datagrid.update", "编辑"),
    },
    service: (params) => {
      return act("saveOrUpdate", {
        params,
        type: "update",
        id: editFormModal.formData?.id,
      }).then((res) => {
        if (res?.success) {
          handleRefresh();
          message.success("编辑成功");
          editFormModal.setVisible(false);
        } else {
          message.error(res?.msg || "编辑失败");
        }
        return res?.success;
      });
    },
  });

  const viewFormModal = useFormModal({
    modal: {
      title: intl.getMessage("datagrid.view", "查看"),
    },
  });

  const handleRefresh = () => {
    datagridProps.onRefresh?.();
  };

  const openView = (record) => {
    viewFormModal.setFormData(record || {});
    viewFormModal.setVisible(true);
    act("view", { id: record.id }).then((data) => {
      if (data && Object.keys(data).length > 0) {
        viewFormModal.setFormData({
          ...record,
          ...data,
        });
      }
    });
  };

  const openEdit = (record) => {
    editFormModal.setFormData(record || {});
    editFormModal.setVisible(true);
    act("view", { id: record.id }).then((data) => {
      if (data && Object.keys(data).length > 0) {
        editFormModal.setFormData({
          ...record,
          ...data,
        });
      }
    });
  };

  const exportColumns = commonColumnParam.map((item) => ({
    title: item[0],
    key: item[1],
    field: item[1],
  }));

  return (
    <TableLayout.Page>
      <TableLayout.Search>
        <Form form={form} name="query-form">
          <VtxSearch titles={["名称", "建设日期", "管理人员"]} gridWeight={[1, 1, 1]} onConfirm={submit} onClear={reset}>
            <Form.Item name="name">
              <VtxInput />
            </Form.Item>
            <Form.Item name="buildDate">
              <DatePicker style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item name="managerStaffId">
              <TreeSelect
                allowClear
                showSearch
                treeNodeFilterProp="title"
                treeData={staffTree}
                placeholder="请选择管理人员"
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
                  <Button
                    icon={<PlusOutlined />}
                    type="primary"
                    onClick={() => {
                      addFormModal.setVisible(true);
                    }}
                  >
                    {intl.getMessage("datagrid.add", "新增")}
                  </Button>
                )}
                {validate("import") && (
                  <Button
                    onClick={() => {
                      setImportVisible(true);
                    }}
                    icon={<ImportOutlined />}
                  >
                    {intl.getMessage("datagrid.import", "导入")}
                  </Button>
                )}
                {validate("export") && (
                  <Export
                    selectedRowKeys={selectedRowKeys}
                    tableData={datagridProps.dataSource || []}
                    params={{
                      ...form.getFieldsValue(),
                      buildDate: form.getFieldValue("buildDate")
                        ? dayjs(form.getFieldValue("buildDate")).format("YYYY-MM-DD")
                        : undefined,
                    }}
                    requestExportUrl="/cloud/sample/example/exportExcel"
                    columns={exportColumns}
                    fileName="样例数据"
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

export default Example;
