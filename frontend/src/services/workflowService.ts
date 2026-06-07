import { client } from "@/utils/client.ts";
import { WorkflowScript, WorkflowScriptsResponse } from "@/interfaces/workflow.ts";
import { WorkflowFunctionType } from "luna";

class WorkflowService {
  getScripts() {
    return client.get<WorkflowScriptsResponse>('/gravity/workflow/scripts');
  }

  setDisabled(workflowId: number, actionId: number, functionType: WorkflowFunctionType, functionId: string, disabled: boolean) {
    return client.post<WorkflowScript>(`/gravity/workflow/scripts`, {
      workflowId,
      actionId,
      functionType,
      functionId,
      disabled,
    });
  }
}

export const workflowService = new WorkflowService();