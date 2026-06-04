import { ProjectInfo, Status, WorkflowFunctionType } from "luna";

export interface WorkflowTransition {
  sourceStatuses: Array<number>,
  targetStatus: number,
  transitionName: number,
}

export interface WorkflowScript {
  id: string,
  workflowId: string,
  workflowName: string,
  originalId: number,
  actionId: string,
  actionName: string,
  script: string,
  scriptNote: string,
  projectKeys: Array<string>,
  functionType: WorkflowFunctionType,
  transition: WorkflowTransition,
}

export interface WorkflowScriptsResponse {
  scripts: Array<WorkflowScript>,
  projects: Record<string, ProjectInfo>,
  statuses: Record<number, Status>,
}

