import u from "updeep";
import { projectService } from "./service";

const initState = {};

export default {
  namespace: "project",
  state: { ...initState },
  effects: {
    page: [
      function* ({ payload = {} }, { call }) {
        const { formData, page, size, sort } = payload;
        const res = yield call(projectService.page, {
          ...formData,
          page: Math.max((page || 1) - 1, 0),
          size,
          sort,
        });
        let dataSource = [];
        let total = 0;
        if (res?.result === 0 && Array.isArray(res?.data?.rows)) {
          dataSource = res.data.rows.map((item) => ({ ...item, key: item.id }));
          total = res.data.total || 0;
        }
        return { total, list: dataSource };
      },
      { type: "takeLatest" },
    ],
    *view({ payload = {} }, { call }) {
      const res = yield call(projectService.view, { id: payload.id });
      if (res?.result === 0) {
        return res?.data || {};
      }
      return {};
    },
    *saveOrUpdate({ payload = {} }, { call }) {
      const { type, params, id } = payload;
      const service = type === "save" ? projectService.save : projectService.update;
      const res = yield call(service, { ...params, id });
      return { success: res?.result === 0, msg: res?.msg || "" };
    },
    *deleteItems({ payload = {} }, { call }) {
      const { ids = [] } = payload;
      const res = yield call(projectService.delete, { ids: ids.join(",") });
      return { success: res?.result === 0, msg: res?.msg || "" };
    },
  },
  reducers: {
    updateState(state, action) {
      return u(action.payload, state);
    },
    initState(state) {
      return u({ ...initState }, state);
    },
  },
};
