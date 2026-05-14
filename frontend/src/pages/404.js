import React from "react";
import { Result } from "antd";

export default function Nodata() {
  return (
    <Result status="404" title="404" subTitle="对不起，你访问的页面不存在" />
  );
}
