import { SearchQuery, SearchResult } from "luna";
import { client } from "@/utils/client.ts";
import { MetricPoint, ScriptRunResult } from "@/interfaces/metrics.ts";

class MetricService {
  getResults(scriptId: string, query: SearchQuery) {
    return client.get<SearchResult<ScriptRunResult>>(`/gravity/scipt/metrics/${scriptId}`, {
      params: {
        page: query.page,
        limit: query.limit,
      }
    });
  }

  getCharPoints(scriptId: string, from: string, to: string) {
    return client.get<Array<MetricPoint>>(`/gravity/scipt/metrics/${scriptId}/points`, {
      params: {
        from,
        to
      }
    });
  }
}

export const metricService = new MetricService();