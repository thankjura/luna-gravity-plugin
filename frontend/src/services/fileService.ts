import { client } from "@/utils/client";
import type { ScriptFileContent, ScriptFileNode } from "@/interfaces/files";
import type { WorkflowScript } from "@/interfaces/workflow";

/** Script files in <luna.home>/scripts. Paths are relative to that directory. */
class FileService {
  getTree() {
    return client.get<ScriptFileNode>('/gravity/files/tree');
  }

  getScripts() {
    return client.get<Array<string>>('/gravity/files/list');
  }

  getContent(path: string) {
    return client.get<ScriptFileContent>('/gravity/files/content', {params: {path}});
  }

  save(path: string, content: string, modified?: number) {
    return client.put<ScriptFileContent>('/gravity/files/content', {path, content, modified});
  }

  createFile(path: string) {
    return client.post<{path: string}>('/gravity/files/file', {path});
  }

  createDirectory(path: string) {
    return client.post<{path: string}>('/gravity/files/directory', {path});
  }

  rename(path: string, newPath: string) {
    return client.post<{path: string}>('/gravity/files/rename', {path, newPath});
  }

  delete(path: string) {
    return client.delete('/gravity/files', {params: {path}});
  }

  getUsages(path: string) {
    return client.get<Array<WorkflowScript>>('/gravity/files/usages', {params: {path}});
  }
}

export const fileService = new FileService();
