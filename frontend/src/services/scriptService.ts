import { baseURL, client } from "@/utils/client.ts";
import * as monaco from 'monaco-editor';
import { Suggestion } from "@/interfaces/script.ts";


class ScriptService {
  async runScript(scriptContent: string, cb: (resp: {log?: string, result?: string, error?: any}) => void) {
    try {
      const response = await fetch(baseURL + '/gravity/script/execute', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({scriptContent})
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

  async getSuggestions(model: monaco.editor.ITextModel, position: monaco.Position): Promise<monaco.languages.ProviderResult<monaco.languages.CompletionList>> {
    const {data} = await client.post<{ suggestions: Array<Suggestion>, range: monaco.IRange }>('/gravity/script/autocomplete', {
      code: model.getValue(),
      line: position.lineNumber,
      column: position.column,
      limit: 20,
    });
    return {
      suggestions: data.suggestions.map((item: Suggestion) => ({
        label: item.label,
        kind: monaco.languages.CompletionItemKind[item.kind] || monaco.languages.CompletionItemKind.Text,
        insertText: item.content,
        detail: item.detail,
        documentation: item.doc,
        range: data.range,
      }))
    }
  }
}

export const scriptService = new ScriptService();