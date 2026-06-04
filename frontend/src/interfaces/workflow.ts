import { ProjectInfo, WorkflowFunctionType } from "luna";

export interface WorkflowScript {
  workflowId: string,
  workflowName: string,
  actionId: string,
  actionName: string,
  script: string,
  functionType: WorkflowFunctionType,
}

export interface WorkflowScriptsResponse {
  scripts: Array<WorkflowScript>,
  projects: Record<string, ProjectInfo>
}

