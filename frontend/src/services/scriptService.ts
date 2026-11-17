import { baseURL, client } from "@/utils/client";
import type * as MonacoEditor from 'monaco-editor';
import type { Diagnostic, SignaturesResponse, SuggestionsResponse } from "@/interfaces/script";


class ScriptService {
  async runScript(scriptContent: string, context: Record<string, string>, cb: (resp: {log?: string, result?: string, error?: any}) => void) {
    try {
      const response = await fetch(baseURL + '/gravity/script/execute', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({scriptContent, context}),
      });

      if (!response.ok) {
        const errorText = await response.text();
        cb({error: errorText});
        return;
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const messages = buffer.split('\n\n');
        buffer = messages.pop() || '';

        for (const message of messages) {
          if (!message.trim()) continue;

          let eventType = 'log';
          let dataContent = '';
          const lines = message.split('\n');
          for (const line of lines) {
            if (line.startsWith('event:')) {
              eventType = line.replace('event:', '').trim();
            } else if (line.startsWith('data:')) {
              dataContent = line.replace('data:', '').trim();
            }
          }
          if (eventType === 'result') {
            cb({ result: dataContent });
          } else {
            cb({ log: dataContent });
          }
        }
      }
    } catch (error) {
      cb({error})
    }
  }

  async getSuggestions(code: string, position: MonacoEditor.IPosition, context: Record<string, string>, signal?: AbortSignal) {
    const {data} = await client.post<SuggestionsResponse>('/gravity/script/autocomplete', {
      code,
      line: position.lineNumber,
      column: position.column,
      context,
    }, {signal});

    return data;
  }

  async getSignatures(code: string, position: MonacoEditor.IPosition, context: Record<string, string>, signal?: AbortSignal) {
    const {data} = await client.post<SignaturesResponse>('/gravity/script/signature', {
      code,
      line: position.lineNumber,
      column: position.column,
      context,
    }, {signal});

    return data;
  }

  async getDiagnostics(code: string, context: Record<string, string>, signal?: AbortSignal) {
    const {data} = await client.post<Array<Diagnostic>>('/gravity/script/diagnostics', {
      code,
      context,
    }, {signal});

    return data;
  }
}

export const scriptService = new ScriptService();