/* eslint-disable react/prop-types */
import React from "react";
import { ExportOutlined } from "@ant-design/icons";
import useNameSpace from "@/hooks/useNameSpace";
import { VtxDatagrid, VtxPageLayout, VtxSearch } from "@vtx/components";
import { Button, DatePicker, Form, Select, TreeSelect, message } from "antd";
import dayjs from "dayjs";
import Export from "./components/Export";
import { API_PREFIX, taskWorkItemService } from "./service";

const { TableLayout, ButtonWrap } = VtxPageLayout;
const { RangePicker } = DatePicker;

const STATUS_OPTIONS = [
  { label: "完成", value: "完成" },
  { label: "延期", value: "延期" },
];

const BASE_COLUMNS = [
  { title: "名称", dataIndex: "name", key: "name", width: 150, align: "left", fixed: "left" },
  { title: "总计", dataIndex: "totalHours", key: "totalHours", width: 90, align: "right", fixed: "right" },
];

const renderWeekTitle = (top, bottom) => (
  <div style={{ lineHeight: 1.2, textAlign: "center" }}>
    <div style={{ color: "#29343d", fontSize: 14, fontWeight: 600 }}>{top}</div>
    <div style={{ color: "#778492", fontSize: 12, fontWeight: 400, marginTop: 4 }}>{bottom}</div>
  </div>
);

function TaskWorkItemOccupancy(props) {
  const {
    title = "人员资源占用表",
    effectName = "weeklyOccupancy",
    exportUrl = `${API_PREFIX}/taskWorkItem/weeklyOccupancy/exportExcel`,
  } = props;
  const { act } = useNameSpace("taskWorkItem");
  const [form] = Form.useForm();
  const [staffTree, setStaffTree] = React.useState([]);
  const [projectOptions, setProjectOptions] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [tableData, setTableData] = React.useState([]);
  const [tableColumns, setTableColumns] = React.useState(BASE_COLUMNS);
  const defaultDateRange = React.useMemo(() => [dayjs().startOf("year"), dayjs()], []);
  const initializedRef = React.useRef(false);

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
      .catch(() => setStaffTree([]));

    taskWorkItemService
      .projectList()
      .then((res) => {
        const rows = Array.isArray(res?.data) ? res.data : [];
        setProjectOptions(
          rows.map((item) => ({
            label: `${item.name || ""}${item.code ? `(${item.code})` : ""}`,
            value: item.id,
          })),
        );
      })
      .catch(() => setProjectOptions([]));
  }, [normalizeTree]);

  const queryWithValues = React.useCallback(
    (values = {}) => {
      const range = values?.startDateRange || [];
      if (!range?.[0] || !range?.[1]) {
        message.warning("请先选择开始日期区间");
        return;
      }
      setLoading(true);
      act(effectName, {
        projectId: values?.projectId,
        ownerTlId: values?.ownerTlId,
        status: values?.status,
        startDateBegin: dayjs(range[0]).format("YYYY-MM-DD"),
        startDateEnd: dayjs(range[1]).format("YYYY-MM-DD"),
      })
        .then((res) => {
          const columns = Array.isArray(res?.columns) ? res.columns : [];
          const rows = Array.isArray(res?.tableData) ? res.tableData : [];

          const nextColumns = columns.map((col) => {
            const isWeekColumn = col.field?.startsWith("week_");
            const isTotalColumn = col.field === "totalHours";
            const isNameColumn = col.field === "name";

            return {
              title: isWeekColumn ? renderWeekTitle(col.titleTop, col.titleBottom) : col.titleTop,
              dataIndex: col.field,
              key: col.field,
              width: isWeekColumn ? 108 : isTotalColumn ? 90 : 150,
              align: isWeekColumn || isTotalColumn ? "right" : "left",
              ellipsis: true,
              fixed: isNameColumn ? "left" : isTotalColumn ? "right" : undefined,
            };
          });
          setTableColumns(nextColumns);

          setTableData(
            rows.map((row) => ({
              ...row,
              totalHours: row.totalHours || 0,
            })),
          );
        })
        .finally(() => setLoading(false));
    },
    [act, effectName],
  );

  const query = React.useCallback(() => {
    queryWithValues(form.getFieldsValue());
  }, [form, queryWithValues]);

  const reset = React.useCallback(() => {
    const defaults = { startDateRange: defaultDateRange };
    form.resetFields();
    form.setFieldsValue(defaults);
    queryWithValues(defaults);
  }, [defaultDateRange, form, queryWithValues]);

  React.useEffect(() => {
    if (initializedRef.current) {
      return;
    }
    initializedRef.current = true;
    const defaults = { startDateRange: defaultDateRange };
    form.setFieldsValue(defaults);
    queryWithValues(defaults);
  }, [defaultDateRange, form, queryWithValues]);

  return (
    <TableLayout.Page>
      <TableLayout.Search>
        <Form form={form} initialValues={{ startDateRange: defaultDateRange }}>
          <VtxSearch titles={["项目名称", "所属TL", "开始日期", "完成状态"]} gridWeight={[1, 1, 1, 1]} onConfirm={query} onClear={reset}>
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
            indexColumn={false}
            reserveScrollBar={true}
            autoFit={true}
            toolbarTilte={title}
            dataSource={tableData}
            rowKey="key"
            columns={Array.isArray(tableColumns) ? tableColumns : []}
            loading={loading}
            pagination={false}
            buttonGroup={
              <ButtonWrap>
                <Export
                  tableData={tableData}
                  params={{
                    ...form.getFieldsValue(),
                    startDateBegin: form.getFieldValue("startDateRange")?.[0]
                      ? dayjs(form.getFieldValue("startDateRange")[0]).format("YYYY-MM-DD")
                      : undefined,
                    startDateEnd: form.getFieldValue("startDateRange")?.[1]
                      ? dayjs(form.getFieldValue("startDateRange")[1]).format("YYYY-MM-DD")
                      : undefined,
                  }}
                  requestExportUrl={exportUrl}
                  columns={[]}
                  fileName={`${title}导出`}
                  trigger={
                    <Button icon={<ExportOutlined />}>
                      导出
                    </Button>
                  }
                />
              </ButtonWrap>
            }
          />
        </TableLayout.Table>
      </TableLayout.Content>
    </TableLayout.Page>
  );
}

export default TaskWorkItemOccupancy;
