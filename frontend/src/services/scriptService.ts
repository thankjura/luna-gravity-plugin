import { CompletionContext } from "@codemirror/autocomplete";
import { baseURL, client } from "@/utils/client.ts";
import { Suggestion } from "@/interfaces/script.ts";
import { EditorView } from "codemirror";


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

  async groovyCompletionSource(context: CompletionContext) {
    try {
      const { data } = await client.post<{from: number, suggestions: Array<Suggestion>}>('/gravity/script/autocomplete', {
        script: context.state.doc.toString(),
        position: context.pos,
        limit: 20,
      });

      return {
        from: data.from,
        options: data.suggestions.map((sug: Suggestion) => ({
          label: sug.name,
          type: sug.type,
          detail: sug.detail,
          apply(view: EditorView, completion: any, from: number, to: number) {
            if (sug.apply && sug.apply.startsWith('AUTO_IMPORT:')) {
              const [, fullClass, simpleName] = sug.apply.split(':');
              const importStatement = `import ${fullClass}\n`;

              const content = view.state.doc.toString();
              let changes = [];
              if (!content.includes(importStatement)) {
                changes.push({ from: 0, insert: importStatement });
              }

              changes.push({ from, to, insert: simpleName });

              view.dispatch({
                changes,
                selection: { anchor: from + simpleName.length + (content.includes(importStatement) ? 0 : importStatement.length) }
              });
            } else {
              view.dispatch({
                changes: { from, to, insert: sug.apply || sug.name }
              });
            }
          }
        }))
      }

    } catch (e) {
      return null;
    }
  }
}

export const scriptService = new ScriptService();