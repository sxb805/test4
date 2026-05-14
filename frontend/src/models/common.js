import u from 'updeep';

export default {
    namespace: 'common',

    state: {},

    effects: {},

    reducers: {
        updateState(state, action) {
            return u(action.payload, state);
        },
    },

    subscriptions: {},
};
