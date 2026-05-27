import React from "react";
import styles from "./index.module.less";

function StatusTag({ text, color }) {
  return (
    <div className={styles.wrap} style={{ backgroundColor: `${color}10`, color, borderColor: color }}>
      {text}
    </div>
  );
}

export default StatusTag;
