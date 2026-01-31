import type * as MonacoEditor from 'monaco-editor';;

export interface Suggestion {
  label: string,
  kind: MonacoEditor.languages.CompletionItemKind,
  detail: string
  insertText: string,
  additionalTextEdits: Array<MonacoEditor.editor.ISingleEditOperation>,
  insertTextRules: MonacoEditor.languages.CompletionItemInsertTextRule
  doc: string,
}

export interface Signature {
  label: string,
  parameters: Array<MonacoEditor.languages.ParameterInformation>
  documentation?: string,
  activeParameter?: number,
}
