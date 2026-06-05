import { SearchQuery, SearchResult } from "luna";
import { client } from "@/utils/client.ts";
import { ScriptRunResult } from "@/interfaces/metrics.ts";

class MetricService {
  getResults(scriptId: string, query: SearchQuery) {
    return client.get<SearchResult<ScriptRunResult>>(`/gravity/scipt/metrics/${scriptId}`, {
      params: {
        page: query.page,
        limit: query.limit,
      }
    });
  }
}

export const metricService = new MetricService();