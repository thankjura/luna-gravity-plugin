import type { IssueEventType, ProjectInfoDto } from "luna";

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

export interface ListenerScriptWithProjectsDto extends ListenerScript {
  projects: Array<ProjectInfoDto>;
}

export interface ListenerScriptsDto {
  scripts: Array<ListenerScript>,
  projects: Array<ProjectInfoDto>,
  eventTypes: Array<IssueEventType>
}

