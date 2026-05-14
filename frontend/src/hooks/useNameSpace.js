import { useDispatch } from "react-redux";

export default (namespace) => {
  const dispatch = useDispatch();
  const updateState = (obj) => {
    dispatch({
      type: `${namespace}/updateState`,
      payload: obj,
    });
  };
  const act = (action, obj = {}, space = namespace) => {
    return dispatch({
      type: `${space}/${action}`,
      payload: obj,
    });
  };
  const getState = (key) => {
    return (state) => state[namespace][key];
  };
  return {
    updateState,
    dispatch,
    act,
    getState,
  };
};
