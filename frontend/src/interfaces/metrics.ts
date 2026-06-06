export interface ScriptRunResult {
  scriptId: string,
  startAt: string,
  executionTimeMs: number,
  cpuTimeMs: number,
  exception: string,
  payload: string,
  logs: string
}