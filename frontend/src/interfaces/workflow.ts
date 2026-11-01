import type { ProjectValueDto, StatusValueDto, WorkflowFunctionType } from "luna";

export interface WorkflowTransition {
  sourceStatuses: Array<number>,
  targetStatus: number,
  transitionName: number,
}

export interface WorkflowScript {
  id: string,
  workflowId: number,
  workflowName: string,
  workflowOriginalId: number,
  actionId: number,
  actionName: string,
  script: string,
  scriptNote: string,
  projectKeys: Array<string>,
  functionType: WorkflowFunctionType,
  transition: WorkflowTransition,
  disabled: boolean,
}

export interface WorkflowScriptsDto {
  scripts: Array<WorkflowScript>,
  projects: Array<ProjectValueDto>,
  statuses: Array<StatusValueDto>,
}

