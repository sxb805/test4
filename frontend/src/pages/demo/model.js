import u from "updeep";
import { getSourceTypeList, userService } from "./service";

const initState = {
  sourceTypeData: [],
};

export default {
  namespace: "user",

  state: { ...initState },

  subscriptions: {
    setup({ history }) {
      return history.listen(() => {
        // no-op
      });
    },
  },

  effects: {
    *loadSourceType(_, { call, put }) {
      const res = yield call(getSourceTypeList);
      if (Array.isArray(res?.data)) {
        yield put({
          type: "updateState",
          payload: {
            sourceTypeData: res.data,
          },
        });
      }
    },

    page: [
      function* ({ payload = {} }, { call }) {
        const { formData, page, size, sort } = payload;
        const res = yield call(userService.page, {
          ...formData,
          page,
          rows: size,
          sort,
        });
        let dataSource = [];
        let total = 0;
        if (res?.result === 0 && Array.isArray(res?.data?.rows)) {
          dataSource = res.data.rows.map((item) => ({
            ...item,
            key: item.id,
          }));
          total = res.data.total || 0;
        }
        return {
          total,
          list: dataSource,
        };
      },
      { type: "takeLatest" },
    ],

    *view({ payload = {} }, { call }) {
      const res = yield call(userService.view, {
        id: payload.id,
      });
      if (res?.result === 0) {
        return res?.data || {};
      }
      return {};
    },

    *saveOrUpdate({ payload = {} }, { call }) {
      const { type, params, id } = payload;
      const service = type === "save" ? userService.save : userService.update;
      const res = yield call(service, {
        ...params,
        id,
      });
      return res?.result === 0;
    },

    *deleteItems({ payload = {} }, { call }) {
      const { ids = [] } = payload;
      const res = yield call(userService.delete, {
        ids: ids.join(","),
      });
      return res?.result === 0;
    },
    *unlockItem({ payload = {} }, { call }) {
      return yield call(userService.unlock, payload);
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
