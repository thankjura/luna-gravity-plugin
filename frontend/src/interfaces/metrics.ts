export interface ScriptRunResult {
  scriptId: string,
  startAt: string,
  executionTimeMs: number,
  cpuTimeMs: number,
  exception: string,
  payload: string,
  logs: string
}

export interface MetricPoint {
  bucket: string,
  totalCount: number,
  avgExecutionTimeMs: number,
  maxExecutionTimeMs: number,
  avgCpuTimeMs: number,
  maxCpuTimeMs: number,
}
