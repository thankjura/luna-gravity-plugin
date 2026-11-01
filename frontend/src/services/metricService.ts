import type { SearchQuery, SearchResult } from "luna";
import { client } from "@/utils/client";
import type { MetricPointCollection, ScriptRunResult } from "@/interfaces/metrics";

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
    return client.get<MetricPointCollection>(`/gravity/scipt/metrics/${scriptId}/points`, {
      params: {
        from,
        to
      }
    });
  }
}

export const metricService = new MetricService();