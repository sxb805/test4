import { useRequest } from "ahooks";
import { useEffect, useState } from "react";
function useOnOk(service) {
  const { runAsync, loading } = useRequest(service, { manual: true });
  return {
    loading,
    onOk: (fieldsValue) => runAsync(fieldsValue),
  };
}

function useFormModal({ modal, service, defaultFormData, confirm, ...rest }) {
  const [visible, setVisible] = useState(false);
  const [formData, setFormData] = useState({});

  useEffect(() => {
    setFormData(defaultFormData || {});
  }, [defaultFormData]);

  const { onCancel, ...restModalProps } = modal;
  const { onOk, loading } = useOnOk(service);
  const modalProps = {
    visible,
    onCancel() {
      setVisible(false);
      setFormData({});
      onCancel && onCancel();
    },
    confirmLoading: loading,
    ...restModalProps,
  };

  return {
    modalProps,
    setVisible,
    formData,
    setFormData,
    confirm: confirm || onOk,
    ...rest,
  };
}

export default useFormModal;
