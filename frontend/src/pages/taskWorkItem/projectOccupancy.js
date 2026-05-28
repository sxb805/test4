import React from "react";
import { API_PREFIX } from "./service";
import TaskWorkItemOccupancy from "./occupancy";

function TaskWorkItemProjectOccupancy() {
  return (
    <TaskWorkItemOccupancy
      title="项目资源占用表"
      effectName="projectWeeklyOccupancy"
      exportUrl={`${API_PREFIX}/taskWorkItem/projectWeeklyOccupancy/exportExcel`}
    />
  );
}

export default TaskWorkItemProjectOccupancy;
