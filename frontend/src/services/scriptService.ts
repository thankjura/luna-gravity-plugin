import axios from 'axios';
import { ScriptResult } from "@/interfaces/script.ts";

const client = axios.create({
  baseURL: '/rest/plugin/ru.slie.luna.plugins.gravity',
})

class ScriptService {
  async runScript(scriptContent: string) {
    return client.post<ScriptResult>('/gravity/scripts/execute', {scriptContent});
  }
}

export const scriptService = new ScriptService();