import React, { useState, useEffect } from "react";
import { Link, Outlet, useAppData, useLocation } from "@umijs/max";
import { Layout, Menu } from "antd";
import { VtxConfigProvider } from "@vtx/components";
import { getUrlParam, stringifyQuerystring } from "@vtx/utils";
import {
  SettingOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import enUS from "antd/locale/en_US";
import zhCN from "antd/locale/zh_CN";
import "dayjs/locale/zh-cn";
import styles from "./index.less";
const locale = {
  enUS,
  zhCN: zhCN,
};
const { Header, Sider, Content } = Layout;
const filterRouteList = ["/403", "*", "/demo"];

const MenuContent = () => {
  const language = localStorage.getItem("vtxLanguage");
  const theme = localStorage.getItem("theme");
  const themeToken = localStorage.getItem("themeToken") ? JSON.parse(localStorage.getItem("themeToken")) : {};

  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const appData = useAppData();
  const routes = appData.routes;
  const routeList = Object.values(routes).filter((item) => {
    return (
      item.parentId == "1" && item.path && !filterRouteList?.includes(item.path)
    );
  });

  useEffect(() => {
    if (language == "zh-CN") {
      dayjs.locale("zh-cn");
    }
    if (language == "en-US") {
      dayjs.locale("en-us");
    }
  }, [language]);

  return (
    <VtxConfigProvider
      dark={theme === "dark"}
      locale={locale[language] || zhCN}
      theme={{
        token: themeToken,
      }}
    >
      <Layout className={styles.layout}>
        <Sider trigger={null} collapsible collapsed={collapsed}>
          <div className={styles.logo} />
          <Menu
            theme="dark"
            mode="inline"
            className={styles.menu}
            selectedKeys={[location.pathname]}
          >
            {routeList.map((item) => {
              return (
                <Menu.Item key={item.path}>
                  <Link
                    key={item.path}
                    to={`${item.path}?${stringifyQuerystring()}`}
                  >
                    <SettingOutlined />
                    <span>{item.title}</span>
                  </Link>
                </Menu.Item>
              );
            })}
          </Menu>
        </Sider>
        <Layout>
          <Header style={{ background: "#fff", padding: 0 }}>
            {collapsed ? (
              <MenuUnfoldOutlined
                className={styles.trigger}
                onClick={() => setCollapsed((oldValue) => !oldValue)}
              />
            ) : (
              <MenuFoldOutlined
                className={styles.trigger}
                onClick={() => setCollapsed((oldValue) => !oldValue)}
              />
            )}
          </Header>
          <Content
            style={{
              margin: "16px 16px",
              background: "#fff",
              minHeight: 280,
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </VtxConfigProvider>
  );
};

export default MenuContent;
