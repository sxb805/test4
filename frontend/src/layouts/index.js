import { useEffect } from "react";
import { Outlet } from "@umijs/max";
import { VtxConfigProvider } from "@vtx/components";
import { getUrlParam } from "@vtx/utils";
import dayjs from "dayjs";
import enUS from "antd/locale/en_US";
import zhCN from "antd/locale/zh_CN";
import "dayjs/locale/zh-cn";

const locale = {
  'en-US': enUS,
  'zh-CN': zhCN,
};

export default function Layout() {
  const language = localStorage.getItem("vtxLanguage");
  const theme = localStorage.getItem("theme");
  const themeToken = localStorage.getItem("themeToken")? JSON.parse(localStorage.getItem("themeToken")) : {};
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
        token:themeToken, 
      }}
    >
      <Outlet />
    </VtxConfigProvider>
  );
}
