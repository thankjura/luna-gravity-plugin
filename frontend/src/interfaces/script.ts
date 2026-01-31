import type * as MonacoEditor from 'monaco-editor';
import { editor } from "monaco-editor";

export interface Suggestion {
  label: string,
  kind: MonacoEditor.languages.CompletionItemKind,
  detail: string
  insertText: string,
  additionalTextEdits: Array<editor.ISingleEditOperation>,
  doc: string,
}