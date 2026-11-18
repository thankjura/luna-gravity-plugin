export interface ScriptFileNode {
  name: string,
  path: string,
  directory: boolean,
  size: number,
  modified: number,
  children?: Array<ScriptFileNode>,
}

export interface ScriptFileContent {
  path: string,
  content: string,
  modified: number,
}
