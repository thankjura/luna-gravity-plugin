import axios from "axios";
import { IssueEventType, Option, Project, SearchQuery, SearchQueryExt, SearchResult } from "luna";

class SystemService {
  getEventTypes() {
    return axios.get<Array<IssueEventType>>("/rest/issue_event_types");
  }

  findProjects(params: SearchQuery, excludes?: Array<string|number>, isAdmin?: boolean) {
    const searchQuery: SearchQueryExt = {...params};
    if (excludes) {
      searchQuery['excludes'] = excludes;
    }
    if (!isAdmin) {
      searchQuery['availableOnly'] = true;
    }
    return axios.post<SearchResult<Project>>("/rest/projects", searchQuery);
  }

  async projectSuggestions(term: string|null, excludes?: Array<number>) {
    const {data} = await this.findProjects({term}, excludes);
    const out = [] as Array<Option<number>>;
    for (let i = 0 ; i < data.results.length; i++) {
      out.push({
        id: data.results[i].id,
        name: data.results[i].name,
        iconUrl: data.results[i].iconUrl,
      });
    }

    return out;
  }

}

export const systemService = new SystemService();