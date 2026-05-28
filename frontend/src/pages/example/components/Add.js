/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { VtxFormLayout, VtxInput, VtxModal, VtxUpload } from "@vtx/components";
import { VtxSearchMapInput } from "@vtx/components-extra";
import { Checkbox, DatePicker, Form, InputNumber, TreeSelect } from "antd";
import dayjs from "dayjs";
import { exampleService } from "../service";

const toDayjs = (value) => {
  if (!value) {
    return undefined;
  }
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed : undefined;
};

const getLngLat = (location) => {
  const lngLats = location?.lngLats || "";
  const [longitude, latitude] = lngLats.split(",");
  return {
    longitude,
    latitude,
  };
};

function Add({ modalProps, formData = {}, confirm }) {
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState([]);
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
  }, [modalProps.visible]);

  useEffect(() => {
    const { longitude, latitude } = getLngLat(formData?.location);
    form.setFieldsValue({
      ...formData,
      buildDate: toDayjs(formData?.buildDate),
      buildTime: toDayjs(formData?.buildTime),
      address: formData?.address,
      longitudeDone: longitude,
      latitudeDone: latitude,
    });
    if (formData?.files) {
      try {
        const parsed = JSON.parse(formData.files);
        setFileList(Array.isArray(parsed) ? parsed : []);
      } catch {
        setFileList([]);
      }
    } else {
      setFileList([]);
    }
  }, [formData, form]);

  useEffect(() => {
    exampleService
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
  }, []);

  const onFinish = (values) => {
    confirm &&
      confirm({
        ...values,
        files: fileList?.length ? JSON.stringify(fileList) : "",
        buildDate: values?.buildDate ? dayjs(values.buildDate).format("YYYY-MM-DD") : undefined,
        buildTime: values?.buildTime
          ? dayjs(values.buildTime).format("YYYY-MM-DD HH:mm:ss")
          : undefined,
        managerStaffName: staffNameMap[values?.managerStaffId] || undefined,
        location:
          values?.longitudeDone && values?.latitudeDone
            ? {
                shapeType: "point",
                coordinateType: "wgs84",
                lngLats: `${values.longitudeDone},${values.latitudeDone}`,
              }
            : undefined,
      });
  };

  return (
    <VtxModal {...modalProps} width={VtxModal.size.large} onOk={() => form.submit()} forceRender>
      <Form form={form} onFinish={onFinish}>
        <VtxFormLayout cols={2}>
          <VtxFormLayout.Card title="样例信息">
            <VtxFormLayout.FormItem label="编码" name="code" rules={[{ required: true, message: "必填" }]}>
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="名称" name="name" rules={[{ required: true, message: "必填" }]}>
              <VtxInput maxLength={128} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="类型" name="type">
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="日期类型" name="dateType">
              <VtxInput maxLength={64} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="建设日期" name="buildDate">
              <DatePicker allowClear style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="建设时间" name="buildTime">
              <DatePicker showTime allowClear style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="是否离线" name="hasOffline" valuePropName="checked">
              <Checkbox />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="管理人员" name="managerStaffId">
              <TreeSelect
                allowClear
                showSearch
                treeNodeFilterProp="title"
                treeData={staffTree}
                placeholder="请选择管理人员"
                treeDefaultExpandAll
              />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="金额" name="amount">
              <InputNumber style={{ width: "100%" }} precision={2} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="版本" name="version">
              <InputNumber style={{ width: "100%" }} />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="地址" name="address" weights={2}>
              <VtxSearchMapInput
                mapProps={{
                  olProps: {
                    olCoverage: window.mapInfo?.olCoverage,
                    projection: window.mapInfo?.projection,
                  },
                  mapType: "olmap",
                }}
                onChange={(address, { longitude, latitude }) => {
                  form.setFieldsValue({
                    longitudeDone: longitude,
                    latitudeDone: latitude,
                    address,
                  });
                }}
                location={[form.getFieldValue("longitudeDone"), form.getFieldValue("latitudeDone")]}
              />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="经度" name="longitudeDone">
              <VtxInput disabled />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="纬度" name="latitudeDone">
              <VtxInput disabled />
            </VtxFormLayout.FormItem>
            <VtxFormLayout.FormItem label="照片" name="files" weights={2} tooltip="上限3张，单个照片小于5M">
              <VtxUpload
                maxNum={3}
                listType="picture-card"
                fileList={fileList}
                onSuccess={(file) => {
                  setFileList([...fileList, { id: file.id, name: file.name, type: file.type }]);
                }}
                onRemove={(file) => {
                  setFileList(fileList.filter((item) => item.id !== file.id));
                }}
                flag={new Date()}
                accept="image/*"
              />
            </VtxFormLayout.FormItem>
          </VtxFormLayout.Card>
        </VtxFormLayout>
      </Form>
    </VtxModal>
  );
}

export default Add;
