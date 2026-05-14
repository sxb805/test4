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
  VtxImport,
  VtxInput,
  VtxPageLayout,
  VtxSearch,
  useIntl,
} from "@vtx/components";
import dayjs from "dayjs";
import { Button, DatePicker, Form, message } from "antd";
import { default as Add, default as Edit } from "./components/Add";
import Export from "./components/Export";
import View from "./components/View";

const { TableLayout, ButtonWrap } = VtxPageLayout;

function Example() {
  const intl = useIntl();
  const { act } = useNameSpace("example");

  const { validate } = usePermission(["add", "edit", "delete", "import", "export"]);

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
    importURL: "/cloud/sample/example/importExcel",
    importTemplateURL: "/resources/template/企业用户导入模板.xlsx",
  });

  const addFormModal = useFormModal({
    modal: {
      title: intl.getMessage("datagrid.add", "新增"),
      width: 700,
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
      width: 700,
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
      width: 700,
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
          <VtxSearch titles={["名称", "建设日期"]} gridWeight={[1, 1]} onConfirm={submit} onClear={reset}>
            <Form.Item name="name">
              <VtxInput />
            </Form.Item>
            <Form.Item name="buildDate">
              <DatePicker style={{ width: "100%" }} />
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
      {importVisible && <VtxImport {...importProps} />}
    </TableLayout.Page>
  );
}

export default Example;
