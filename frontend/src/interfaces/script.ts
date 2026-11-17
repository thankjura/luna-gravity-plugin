import type * as MonacoEditor from 'monaco-editor';

export interface Suggestion {
  label: string,
  labelDetail?: string,
  labelDescription?: string,
  kind: MonacoEditor.languages.CompletionItemKind,
  detail?: string
  insertText: string,
  additionalTextEdits?: Array<MonacoEditor.editor.ISingleEditOperation>,
  insertTextRules?: MonacoEditor.languages.CompletionItemInsertTextRule
  doc?: string,
  sortText?: string,
  filterText?: string,
  triggerSignature?: boolean,
}

export interface SuggestionsResponse {
  suggestions: Array<Suggestion>,
  incomplete: boolean,
}

export interface Signature {
  label: string,
  parameters: Array<MonacoEditor.languages.ParameterInformation>
  documentation?: string,
  activeParameter?: number,
}

export interface SignaturesResponse {
  signatures: Array<Signature>,
  activeSignature: number,
  activeParameter: number,
}

export interface QuickFix {
  title: string,
  edits: Array<MonacoEditor.editor.ISingleEditOperation>,
  preferred: boolean,
}

export interface Diagnostic {
  range: MonacoEditor.IRange,
  message: string,
  severity: MonacoEditor.MarkerSeverity,
  fixes: Array<QuickFix>,
}
