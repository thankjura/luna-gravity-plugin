import type { IssueEventType, ProjectInfo } from "luna";

export interface ListenerScript {
  id: number,
  name: string,
  description: string,
  projectIds: Array<number>,
  eventTypeIds: Array<number>,
  script: string,
  async: boolean,
  enabled: boolean,
}

export interface ListenerScriptWithProjects extends ListenerScript {
  projects: Array<ProjectInfo>;
}

export interface ListenerScriptsResponse {
  scripts: Array<ListenerScript>,
  projects: Array<ProjectInfo>,
  eventTypes: Array<IssueEventType>
}

