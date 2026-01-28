import { I18N } from "luna";
const loadI18N = async (locale: string) => {
  try {
    const {messages} = await import(`./locales/messages_${locale}.ts`);
    return messages;
  } catch (e) {
    return {};
  }
}

export const $i18n = new I18N(['ru'], loadI18N);