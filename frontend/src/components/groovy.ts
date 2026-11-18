import { scriptService } from "@/services/scriptService";
import type * as MonacoEditor from 'monaco-editor';
import type { Diagnostic, Suggestion } from "@/interfaces/script";

function getTokens(tokens: string, divider = "|"): string[] {
  return tokens.split(divider);
}

const wordPattern = /(-?\d*\.\d\w*)|([a-zA-Z_$][\w$]*)/g;

const brackets: MonacoEditor.languages.CharacterPair[] = [
  ["{", "}"],
  ["[", "]"],
  ["(", ")"],
];

const autoClosingPairs = [
  { open: "{", close: "}" },
  { open: "[", close: "]" },
  { open: "(", close: ")" },
  { open: '"', close: '"' },
  { open: "'", close: "'" },
  { open: "`", close: "`" },
];

const surroundingPairs = autoClosingPairs;

const id = "groovy";
const label = "Groovy";

const MARKER_OWNER = "gravity";
const MARKER_SOURCE = "groovy";

type ContextGetter = () => Record<string, string> | undefined;

/** Script context (available variables) per editor model: providers are registered once per language. */
const modelContexts = new Map<string, ContextGetter>();
/** Last diagnostics per model, used by quick fixes. */
const modelDiagnostics = new Map<string, Array<Diagnostic>>();

const contextOf = (model: MonacoEditor.editor.ITextModel) => modelContexts.get(model.uri.toString())?.() || {};

const abortOn = (token: MonacoEditor.CancellationToken) => {
  const controller = new AbortController();
  token.onCancellationRequested(() => controller.abort());
  return controller.signal;
}

export const attachModel = (model: MonacoEditor.editor.ITextModel, getContext: ContextGetter) => {
  modelContexts.set(model.uri.toString(), getContext);
}

export const detachModel = (model: MonacoEditor.editor.ITextModel) => {
  modelContexts.delete(model.uri.toString());
  modelDiagnostics.delete(model.uri.toString());
}

let providersRegistered = false;

export const registerGroovyProviders = (instance: typeof MonacoEditor) => {
  if (providersRegistered) {
    return;
  }
  providersRegistered = true;

  instance.languages.registerCompletionItemProvider(id, {
    triggerCharacters: ["."],
    provideCompletionItems: async (model, position, _context, token) => {
      const word = model.getWordUntilPosition(position);
      const fullWord = model.getWordAtPosition(position);
      const insert = new instance.Range(position.lineNumber, word.startColumn, position.lineNumber, position.column);
      const replace = new instance.Range(position.lineNumber, word.startColumn, position.lineNumber, fullWord ? fullWord.endColumn : position.column);

      try {
        const data = await scriptService.getSuggestions(model.getValue(), position, contextOf(model), abortOn(token));
        return {
          incomplete: data.incomplete,
          suggestions: data.suggestions.map((item: Suggestion) => ({
            label: {label: item.label, detail: item.labelDetail, description: item.labelDescription},
            kind: item.kind,
            insertText: item.insertText,
            insertTextRules: item.insertTextRules,
            detail: item.detail,
            documentation: item.doc ?? undefined,
            sortText: item.sortText,
            filterText: item.filterText,
            additionalTextEdits: item.additionalTextEdits,
            range: {insert, replace},
            command: item.triggerSignature ? {id: "editor.action.triggerParameterHints", title: ""} : undefined,
          }))
        };
      } catch (e) {
        return {suggestions: []};
      }
    }
  });

  instance.languages.registerSignatureHelpProvider(id, {
    signatureHelpTriggerCharacters: ["(", ","],
    signatureHelpRetriggerCharacters: [","],
    provideSignatureHelp: async (model, position, token) => {
      try {
        const data = await scriptService.getSignatures(model.getValue(), position, contextOf(model), abortOn(token));
        if (!data.signatures.length) {
          return null;
        }
        // Monaco renders a non-string documentation as markdown and fails on null: drop nulls
        const value: MonacoEditor.languages.SignatureHelp = {
          activeSignature: data.activeSignature,
          activeParameter: data.activeParameter,
          signatures: data.signatures.map(signature => ({
            label: signature.label,
            documentation: signature.documentation ?? undefined,
            activeParameter: signature.activeParameter ?? undefined,
            parameters: (signature.parameters ?? []).map(p => ({label: p.label, documentation: p.documentation ?? undefined})),
          })),
        };
        return {value, dispose: () => {}};
      } catch (e) {
        return null;
      }
    }
  });

  instance.languages.registerCodeActionProvider(id, {
    provideCodeActions: (model, _range, context) => {
      const diagnostics = modelDiagnostics.get(model.uri.toString()) || [];
      const actions: Array<MonacoEditor.languages.CodeAction> = [];
      for (const marker of context.markers) {
        if (marker.source !== MARKER_SOURCE) {
          continue;
        }
        const diagnostic = diagnostics.find(d => d.message === marker.message
            && d.range.startLineNumber === marker.startLineNumber && d.range.startColumn === marker.startColumn);
        for (const fix of diagnostic?.fixes || []) {
          actions.push({
            title: fix.title,
            kind: "quickfix",
            isPreferred: fix.preferred,
            diagnostics: [marker],
            edit: {
              edits: fix.edits.map(edit => ({
                resource: model.uri,
                textEdit: edit,
                versionId: model.getVersionId(),
              }))
            }
          });
        }
      }
      return {actions, dispose: () => {}};
    }
  }, {providedCodeActionKinds: ["quickfix"]});
}

/**
 * Highlights script errors while typing (debounced). Returns a disposable that clears markers.
 */
export const watchDiagnostics = (instance: typeof MonacoEditor, editor: MonacoEditor.editor.IStandaloneCodeEditor, delay = 600) => {
  let timer: ReturnType<typeof setTimeout> | undefined;
  let controller: AbortController | undefined;

  const run = async () => {
    const model = editor.getModel();
    if (!model) {
      return;
    }
    controller?.abort();
    controller = new AbortController();
    const version = model.getVersionId();
    try {
      const diagnostics = await scriptService.getDiagnostics(model.getValue(), contextOf(model), controller.signal);
      if (model.isDisposed() || model.getVersionId() !== version) {
        return;
      }
      modelDiagnostics.set(model.uri.toString(), diagnostics);
      instance.editor.setModelMarkers(model, MARKER_OWNER, diagnostics.map(d => ({
        ...d.range,
        message: d.message,
        severity: d.severity,
        source: MARKER_SOURCE,
      })));
    } catch (e) {
      // request aborted or failed: keep previous markers
    }
  };

  const schedule = () => {
    clearTimeout(timer);
    timer = setTimeout(run, delay);
  };

  const listener = editor.onDidChangeModelContent(schedule);
  schedule();

  return {
    dispose: () => {
      clearTimeout(timer);
      controller?.abort();
      listener.dispose();
      const model = editor.getModel();
      if (model && !model.isDisposed()) {
        instance.editor.setModelMarkers(model, MARKER_OWNER, []);
      }
    }
  };
}

export const registerGroovyLanguageForMonaco = (instance: typeof MonacoEditor) => {
  const languages = instance.languages.getLanguages();
    if (languages.some(lang => lang.id === id)) {
      return;
    }

  instance.languages.register({ id, aliases: [label] });
  instance.languages.setMonarchTokensProvider(id, {
    tokenPostfix: ".groovy",
    keywords: getTokens(
        "assert|with|abstract|continue|for|new|switch|assert|default|goto|package|synchronized|boolean|do|if|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|def|float|native|super|while|in|as"
    ),
    typeKeywords: getTokens(
        "Long|Integer|Short|Byte|Double|Number|Float|Character|Boolean|StackTraceElement|Appendable|StringBuffer|Iterable|ThreadGroup|Runnable|Thread|IllegalMonitorStateException|StackOverflowError|OutOfMemoryError|VirtualMachineError|ArrayStoreException|ClassCastException|LinkageError|NoClassDefFoundError|ClassNotFoundException|RuntimeException|Exception|ThreadDeath|Error|Throwable|System|ClassLoader|Cloneable|Class|CharSequence|Comparable|String|Object"
    ),
    constants: getTokens("null|Infinity|NaN|undefined|true|false"),
    builtinFunctions: getTokens(
        "AbstractMethodError|AssertionError|ClassCircularityError|ClassFormatError|Deprecated|EnumConstantNotPresentException|ExceptionInInitializerError|IllegalAccessError|IllegalThreadStateException|InstantiationError|InternalError|NegativeArraySizeException|NoSuchFieldError|Override|Process|ProcessBuilder|SecurityManager|StringIndexOutOfBoundsException|SuppressWarnings|TypeNotPresentException|UnknownError|UnsatisfiedLinkError|UnsupportedClassVersionError|VerifyError|InstantiationException|IndexOutOfBoundsException|ArrayIndexOutOfBoundsException|CloneNotSupportedException|NoSuchFieldException|IllegalArgumentException|NumberFormatException|SecurityException|Void|InheritableThreadLocal|IllegalStateException|InterruptedException|NoSuchMethodException|IllegalAccessException|UnsupportedOperationException|Enum|StrictMath|Package|Compiler|Readable|Runtime|StringBuilder|Math|IncompatibleClassChangeError|NoSuchMethodError|ThreadLocal|RuntimePermission|ArithmeticException|NullPointerException"
    ),
    operators: [
      ".",
      ".&",
      ".@",
      "?.",
      "*",
      "*.",
      "*:",
      "~",
      "!",
      "++",
      "--",
      "**",
      "+",
      "-",
      "*",
      "/",
      "%",
      "<<",
      ">>",
      ">>>",
      "..",
      "..<",
      "<",
      "<=",
      ">",
      ">=",
      "==",
      "!=",
      "<=>",
      "===",
      "!==",
      "=~",
      "==~",
      "^",
      "|",
      "&&",
      "||",
      "?",
      ":",
      "?:",
      "=",
      "**=",
      "*=",
      "/=",
      "%=",
      "+=",
      "-=",
      "<<=",
      ">>=",
      ">>>=",
      "&=",
      "^=",
      "|=",
      "?=",
    ],
    symbols: /[=><!~?:&|+\-*/^%]+/,
    escapes: /\\(?:[abfnrtv\\"'`]|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

    regexpctl: /[(){}[\]$^|\-*+?.]/,
    regexpesc: /\\(?:[bBdDfnrstvwWn0\\/]|@regexpctl|c[A-Z]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4})/,

    tokenizer: {
      root: [
        { include: "@whitespace" },
        [
          /\/(?=([^\\/]|\\.)+\/([dgimsuy]*)(\s*)(\.|;|,|\)|\]|\}|$))/,
          { token: "regexp", bracket: "@open", next: "@regexp" },
        ],
        { include: "@comments" },
        { include: "@numbers" },
        { include: "common" },
        [/[;,.]/, "delimiter"],
        [/[(){}[\]]/, "@brackets"],
        [
          /[a-zA-Z_$]\w*/,
          {
            cases: {
              "@keywords": "keyword",
              "@typeKeywords": "type",
              "@constants": "constant.groovy",
              "@builtinFunctions": "constant.other.color",
              "@default": "identifier",
            },
          },
        ],
        [
          /@symbols/,
          {
            cases: {
              "@operators": "operator",
              "@default": "",
            },
          },
        ],
      ],
      common: [
        // delimiters and operators
        [/[()[\]]/, "@brackets"],
        [/[<>](?!@symbols)/, "@brackets"],
        [
          /@symbols/,
          {
            cases: {
              "@operators": "delimiter",
              "@default": "",
            },
          },
        ],

        [
          /\/(?=([^\\/]|\\.)+\/([gimsuy]*)(\s*)(\.|;|\/|,|\)|\]|\}|$))/,
          { token: "regexp", bracket: "@open", next: "@regexp" },
        ],

        // delimiter: after number because of .\d floats
        [/[;,.]/, "delimiter"],

        // strings
        [/"([^"\\]|\\.)*$/, "string.invalid"],
        [/'([^'\\]|\\.)*$/, "string.invalid"],
        [/"/, "string", "@string_double"],
        [/'/, "string", "@string_single"],
      ],
      whitespace: [[/\s+/, "white"]],
      comments: [
        [/\/\/.*/, "comment"],
        [
          /\/\*/,
          {
            token: "comment.quote",
            next: "@comment",
          },
        ],
      ],
      comment: [
        [/[^*/]+/, "comment"],
        [
          /\*\//,
          {
            token: "comment.quote",
            next: "@pop",
          },
        ],
        [/./, "comment"],
      ],
      commentAnsi: [
        [
          /\/\*/,
          {
            token: "comment.quote",
            next: "@comment",
          },
        ],
        [/[^*/]+/, "comment"],
        [
          /\*\//,
          {
            token: "comment.quote",
            next: "@pop",
          },
        ],
        [/./, "comment"],
      ],
      numbers: [
        [/[+-]?\d+(?:(?:\.\d*)?(?:[eE][+-]?\d+)?)?f?\b/, "number.float"],
        [/[+-]?(?:0[obx])?\d+(?:u?[lst]?)?\b/, "number"],
      ],
      regexp: [
        [/(\{)(\d+(?:,\d*)?)(\})/, ["regexp.escape.control", "regexp.escape.control", "regexp.escape.control"]],
        [
          /(\[)(\^?)(?=(?:[^\]\\/]|\\.)+)/,
          // @ts-ignore
          ["regexp.escape.control", { token: "regexp.escape.control", next: "@regexrange" }],
        ],
        [/(\()(\?:|\?=|\?!)/, ["regexp.escape.control", "regexp.escape.control"]],
        [/[()]/, "regexp.escape.control"],
        [/@regexpctl/, "regexp.escape.control"],
        [/[^\\/]/, "regexp"],
        [/@regexpesc/, "regexp.escape"],
        [/\\\./, "regexp.invalid"],
        // @ts-ignore
        [/(\/)([gimsuy]*)/, [{ token: "regexp", bracket: "@close", next: "@pop" }, "keyword.other"]],
      ],

      regexrange: [
        [/-/, "regexp.escape.control"],
        [/\^/, "regexp.invalid"],
        [/@regexpesc/, "regexp.escape"],
        [/[^\]]/, "regexp"],
        [/\]/, { token: "regexp.escape.control", next: "@pop", bracket: "@close" }],
      ],
      embedded: [
        [
          /([^@]|^)([@]{4})*[@]{2}([@]([^@]|$)|[^@]|$)/,
          {
            token: "@rematch",
            next: "@pop",
            nextEmbedded: "@pop",
          },
        ],
      ],
      string_double: [
        [/\$\{/, { token: "delimiter.bracket", next: "@bracketCounting" }],
        [/[^\\"$]+/, "string"],
        [/[^\\"]+/, "string"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [/"/, "string", "@pop"],
      ],
      string_single: [
        [/[^\\']+/, "string"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [/'/, "string", "@pop"],
      ],
      string_backtick: [
        [/\$\{/, { token: "delimiter.bracket", next: "@bracketCounting" }],
        [/[^\\"$]+/, "string"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [/"/, "string", "@pop"],
      ],
      bracketCounting: [
        [/\{/, "delimiter.bracket", "@bracketCounting"],
        [/\}/, "delimiter.bracket", "@pop"],
        { include: "common" },
      ],
    },
  });
  instance.languages.setLanguageConfiguration(id, {
    comments: {
      lineComment: "//",
      blockComment: ["/*", "*/"],
    },
    brackets,
    autoClosingPairs,
    surroundingPairs,
    wordPattern,
  });
};