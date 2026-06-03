import { client } from "@/utils/client.ts";
import { WorkflowScript } from "@/interfaces/workflow.ts";

class WorkflowService {
  getScripts() {
    return client.get<Array<WorkflowScript>>('/gravity/workflow/scripts');
  }
}

export const workflowService = new WorkflowService();