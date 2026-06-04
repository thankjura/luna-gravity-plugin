import { client } from "@/utils/client.ts";
import { WorkflowScriptsResponse } from "@/interfaces/workflow.ts";

class WorkflowService {
  getScripts() {
    return client.get<WorkflowScriptsResponse>('/gravity/workflow/scripts');
  }
}

export const workflowService = new WorkflowService();