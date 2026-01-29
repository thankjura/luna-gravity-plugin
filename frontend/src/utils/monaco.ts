import loader from '@monaco-editor/loader';

export const loadMonacoInstance = async () => {
  const scriptBase = import.meta.url;
  const monacoBaseUrl = new URL('../editor/vs', scriptBase).href;

  loader.config({
    paths: {
      vs: monacoBaseUrl
    }
  });

  return loader.init();
}
