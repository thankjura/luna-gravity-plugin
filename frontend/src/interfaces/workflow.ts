export enum WorkflowFunctionType {
  CONDITION = "condition",
  VALIDATOR = "validator",
  POSTFUNCTION = "postfunction",
}

export interface WorkflowScript {
  workflowId: string,
  workflowName: string,
  actionId: string,
  actionName: string,
  script: string,
  functionType: string,
}