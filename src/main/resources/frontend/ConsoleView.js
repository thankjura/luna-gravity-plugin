import { _ as _sfc_main$1 } from "./CodeEditor.vue_vue_type_script_setup_true_lang-BDllYHR0.js";
const axios = window["axios"];
const client = axios.create({
  baseURL: "/rest/plugin/ru.slie.luna.plugins.gravity"
});
class ScriptService {
  async runScript(scriptContent) {
    return client.post("/gravity/scripts/execute", { scriptContent });
  }
}
const scriptService = new ScriptService();
const _defineComponent = window["Vue"].defineComponent;
const _createElementVNode = window["Vue"].createElementVNode;
const _createVNode = window["Vue"].createVNode;
const _createTextVNode = window["Vue"].createTextVNode;
const _unref = window["Vue"].unref;
const _withCtx = window["Vue"].withCtx;
const _toDisplayString = window["Vue"].toDisplayString;
const _openBlock = window["Vue"].openBlock;
const _createElementBlock = window["Vue"].createElementBlock;
const _createCommentVNode = window["Vue"].createCommentVNode;
const _hoisted_1 = { class: "demo-page pad" };
const _hoisted_2 = { key: 0 };
const ref = window["Vue"].ref;
const ButtonBusy = window["__LUNA_COMPONENTS__"].ButtonBusy;
const _sfc_main = /* @__PURE__ */ _defineComponent({
  __name: "ConsoleView",
  setup(__props) {
    const value = ref(null);
    const busy = ref(false);
    const result = ref(null);
    const runScript = () => {
      if (!value.value) {
        return;
      }
      busy.value = true;
      scriptService.runScript(value.value).then((data) => {
        result.value = data.data;
      }).catch((e) => {
        console.log(e);
      }).finally(() => {
        busy.value = false;
      });
    };
    return (_ctx, _cache) => {
      return _openBlock(), _createElementBlock("div", _hoisted_1, [
        _cache[2] || (_cache[2] = _createElementVNode("h3", null, "Demo page", -1)),
        _createVNode(_sfc_main$1, {
          modelValue: value.value,
          "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => value.value = $event)
        }, null, 8, ["modelValue"]),
        _createVNode(_unref(ButtonBusy), {
          busy: busy.value,
          disabled: busy.value,
          onClick: runScript
        }, {
          default: _withCtx(() => [..._cache[1] || (_cache[1] = [
            _createTextVNode("Выполнить", -1)
          ])]),
          _: 1
        }, 8, ["busy", "disabled"]),
        result.value ? (_openBlock(), _createElementBlock("span", _hoisted_2, _toDisplayString(result.value.result), 1)) : _createCommentVNode("", true)
      ]);
    };
  }
});
export {
  _sfc_main as default
};
//# sourceMappingURL=ConsoleView.js.map
