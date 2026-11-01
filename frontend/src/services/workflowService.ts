import { client } from "@/utils/client";
import type { WorkflowScript, WorkflowScriptsDto } from "@/interfaces/workflow";
import type { WorkflowFunctionType } from "luna";

class WorkflowService {
  getScripts() {
    return client.get<WorkflowScriptsDto>('/gravity/workflow/scripts');
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