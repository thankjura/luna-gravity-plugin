import { _ as _sfc_main$1, s as scriptService } from "./CodeEditor.vue_vue_type_style_index_0_lang-DqeqcgE0.js";
const __variableDynamicImportRuntimeHelper = (glob$1, path$13, segs) => {
  const v = glob$1[path$13];
  if (v) return typeof v === "function" ? v() : Promise.resolve(v);
  return new Promise((_, reject) => {
    (typeof queueMicrotask === "function" ? queueMicrotask : setTimeout)(reject.bind(null, /* @__PURE__ */ new Error("Unknown variable dynamic import: " + path$13 + (path$13.split("/").length !== segs ? ". Note that variables only represent file names one level deep." : ""))));
  });
};
const I18N = window["__LUNA_COMPONENTS__"].I18N;
const loadI18N = async (locale) => {
  try {
    const { messages } = await __variableDynamicImportRuntimeHelper(/* @__PURE__ */ Object.assign({}), `./locales/messages_${locale}.ts`, 3);
    return messages;
  } catch (e) {
    return {};
  }
};
const $i18n = new I18N(["ru"], loadI18N);
const _defineComponent = window["Vue"].defineComponent;
const _createElementVNode = window["Vue"].createElementVNode;
const _createVNode = window["Vue"].createVNode;
const _unref = window["Vue"].unref;
const _toDisplayString = window["Vue"].toDisplayString;
const _createTextVNode = window["Vue"].createTextVNode;
const _withCtx = window["Vue"].withCtx;
const _normalizeClass = window["Vue"].normalizeClass;
const _renderList = window["Vue"].renderList;
const _Fragment = window["Vue"].Fragment;
const _openBlock = window["Vue"].openBlock;
const _createElementBlock = window["Vue"].createElementBlock;
const _createCommentVNode = window["Vue"].createCommentVNode;
const _hoisted_1 = { class: "gravity-page gravity-page-console pad" };
const _hoisted_2 = { class: "pad panel editor-panel" };
const _hoisted_3 = { class: "tabs-header no-border" };
const _hoisted_4 = {
  key: 0,
  class: "panel pad log-tab"
};
const _hoisted_5 = {
  key: 0,
  class: "error"
};
const _hoisted_6 = {
  key: 1,
  class: "panel pad result-tab"
};
const inject = window["Vue"].inject;
const ref = window["Vue"].ref;
const ButtonBusy = window["__LUNA_COMPONENTS__"].ButtonBusy;
const LIMIT = 300;
const _sfc_main = /* @__PURE__ */ _defineComponent({
  __name: "ConsoleView",
  setup(__props) {
    const value = ref('System.out.println("Hello");\nlog.info("hello world")');
    const busy = ref(false);
    const out = ref([]);
    const activeTab = ref("result");
    const result = ref(null);
    const $notify = inject("$notify");
    const errors = ref({});
    const onResultFetch = (resp) => {
      if (resp.log) {
        out.value.push(resp.log);
      }
      if (out.value.length > LIMIT) {
        out.value.shift();
      }
      if (resp.result != void 0) {
        result.value = resp.result;
        busy.value = false;
        activeTab.value = "result";
      }
      if (resp.error) {
        busy.value = false;
        $notify.error($i18n.t("Failed to execute script"));
        if (typeof resp.error == "string") {
          try {
            const data = JSON.parse(resp.error);
            if (data.errors) {
              errors.value = data.errors;
            }
          } catch (e) {
          }
        }
      }
    };
    const runScript = () => {
      if (!value.value) {
        return;
      }
      result.value = null;
      out.value = [];
      activeTab.value = "log";
      busy.value = true;
      scriptService.runScript(value.value, onResultFetch);
    };
    return (_ctx, _cache) => {
      return _openBlock(), _createElementBlock("div", _hoisted_1, [
        _cache[3] || (_cache[3] = _createElementVNode("nav", { class: "breadcrumbs header" }, [
          _createElementVNode("ol", { role: "list" }, [
            _createElementVNode("li", null, "Gravity"),
            _createElementVNode("li", null, "Console")
          ])
        ], -1)),
        _createElementVNode("div", _hoisted_2, [
          _createVNode(_sfc_main$1, {
            modelValue: value.value,
            "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => value.value = $event),
            diabled: busy.value
          }, null, 8, ["modelValue", "diabled"]),
          _createVNode(_unref(ButtonBusy), {
            busy: busy.value,
            disabled: busy.value,
            onClick: runScript
          }, {
            default: _withCtx(() => [
              _createTextVNode(_toDisplayString(_unref($i18n).t("Execute")), 1)
            ]),
            _: 1
          }, 8, ["busy", "disabled"])
        ]),
        _createElementVNode("div", _hoisted_3, [
          _createElementVNode("ul", null, [
            _createElementVNode("li", {
              class: _normalizeClass({ active: activeTab.value == "result" })
            }, [
              _createElementVNode("button", {
                type: "button",
                onClick: _cache[1] || (_cache[1] = ($event) => activeTab.value = "result")
              }, "Result")
            ], 2),
            _createElementVNode("li", {
              class: _normalizeClass({ active: activeTab.value == "log" })
            }, [
              _createElementVNode("button", {
                type: "button",
                onClick: _cache[2] || (_cache[2] = ($event) => activeTab.value = "log")
              }, "Log")
            ], 2)
          ])
        ]),
        activeTab.value == "log" ? (_openBlock(), _createElementBlock("div", _hoisted_4, [
          (_openBlock(true), _createElementBlock(_Fragment, null, _renderList(out.value, (line, idx) => {
            return _openBlock(), _createElementBlock("div", { key: idx }, _toDisplayString(line), 1);
          }), 128)),
          errors.value.message ? (_openBlock(), _createElementBlock("div", _hoisted_5, _toDisplayString(errors.value.message), 1)) : _createCommentVNode("", true)
        ])) : _createCommentVNode("", true),
        activeTab.value == "result" ? (_openBlock(), _createElementBlock("div", _hoisted_6, _toDisplayString(result.value), 1)) : _createCommentVNode("", true)
      ]);
    };
  }
});
const _export_sfc = (sfc, props) => {
  const target = sfc.__vccOpts || sfc;
  for (const [key, val] of props) {
    target[key] = val;
  }
  return target;
};
const ConsoleView = /* @__PURE__ */ _export_sfc(_sfc_main, [["__scopeId", "data-v-f3e309f6"]]);
export {
  ConsoleView as default
};
//# sourceMappingURL=ConsoleView.js.map
