/**
 * permissions:
 *  add: 新增
 *  edit: 编辑
 *  export: 导出
 *  delete: 删除
 *  import: 导入
 */
import React, { useEffect } from "react";

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
  VtxSelect,
  useIntl,
} from "@vtx/components";
import dayjs from "dayjs";
import { Button, Form, message } from "antd";
import { useSelector } from "react-redux";
import { default as Add, default as Edit } from "./components/Add";
import Export from "./components/Export";
import View from "./components/View";

const { TableLayout, ButtonWrap } = VtxPageLayout;
const Option = VtxSelect.Option;

const stateOptions = [
  { code: 1, value: "启用" },
  { code: 0, value: "注销" },
];

function User() {
  const intl = useIntl();
  const { act, getState } = useNameSpace("user");
  const sourceTypeData = useSelector(getState("sourceTypeData"));

  const { validate } = usePermission(["add", "edit", "delete", "import", "export"]);

  useEffect(() => {
    act("initState");
    act("loadSourceType");
  }, []);

  const commonColumnParam = [
    ["登录账号", "username"],
    ["用户姓名", "realName"],
    ["状态", "stateName"],
    [
      "注册时间",
      "registerDate",
      {
        render(text) {
          return text ? dayjs(text).format("YYYY-MM-DD") : "";
        },
      },
    ],
    ["用户来源", "sourceTypeValue"],
  ];

  const columnParam = [
    ...commonColumnParam,
    [
      "操作",
      "action",
      {
        width: 240,
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
            {
              name: "解锁",
              onClick() {
                handleUnlock(record);
              },
              visible: validate("edit") && record.locked === true,
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
      return formData;
    },
    importURL: "/cloud/wfcs/web/api/companyuser/import",
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
      }).then((status) => {
        if (status) {
          handleRefresh();
          message.success("新增成功");
          addFormModal.setVisible(false);
        }
        return status;
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
      }).then((status) => {
        if (status) {
          handleRefresh();
          message.success("编辑成功");
          editFormModal.setVisible(false);
        }
        return status;
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

  const handleUnlock = (record) => {
    act("unlockItem", { id: record.id }).then((res) => {
      if (res?.result === 0) {
        message.success(res?.msg || "解锁成功");
        handleRefresh();
      } else {
        message.error(res?.msg || "操作失败");
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
          <VtxSearch
            titles={["登录账号", "用户姓名", "状态", "用户来源"]}
            gridWeight={[1, 1, 1, 1]}
            onConfirm={submit}
            onClear={reset}
          >
            <Form.Item name="usernameLike">
              <VtxInput />
            </Form.Item>
            <Form.Item name="realName">
              <VtxInput />
            </Form.Item>
            <Form.Item name="state">
              <VtxSelect allowClear>
                {stateOptions.map((item) => (
                  <Option key={item.code} value={item.code}>
                    {item.value}
                  </Option>
                ))}
              </VtxSelect>
            </Form.Item>
            <Form.Item name="sourceTypeKey">
              <VtxSelect allowClear>
                {sourceTypeData.map((item) => (
                  <Option key={item.parmCode} value={item.parmCode}>
                    {item.parmName}
                  </Option>
                ))}
              </VtxSelect>
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
                    params={form.getFieldsValue()}
                    requestExportUrl="/cloud/wfcs/web/api/companyuser/export"
                    columns={exportColumns}
                    fileName="企业用户"
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

export default User;
