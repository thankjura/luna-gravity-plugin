export interface Suggestion {
  label: string,
  kind: 'Method'|'Property'|'Class'|'Keyword'|'Variable'|string,
  detail: string
  content: string,
  apply: string,
  doc: string,
  extra: string
}