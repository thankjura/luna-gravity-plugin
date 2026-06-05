import { Directive } from "vue";

interface LazyElement extends HTMLElement {
  _lazyTimer?: number;
  _lazyObserver?: IntersectionObserver;
}

interface LazyLoadConfig {
  handler: () => void;
  delay?: number;
}

type LazyLoadBindingValue = LazyLoadConfig | (() => void);

export const vLazyLoad: Directive<LazyElement, LazyLoadBindingValue> = {
  mounted(el, binding) {
    let handler: () => void;
    let delay = 1000;
    if (typeof binding.value === 'function') {
      handler = binding.value;
    } else if (binding.value && typeof binding.value === 'object') {
      handler = binding.value.handler;
      if (binding.value.delay !== undefined) {
        delay = binding.value.delay;
      }
    } else {
      return;
    }

    if (typeof binding.value !== 'function') {
      return;
    }

    const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              el._lazyTimer = window.setTimeout(() => {
                handler();
                observer.unobserve(el);
              }, delay);
            } else {
              if (el._lazyTimer) {
                clearTimeout(el._lazyTimer);
              }
            }
          });
        },
        {
          root: null,
          threshold: 0.5,
        }
    );

    observer.observe(el);
    el._lazyObserver = observer;
  },

  unmounted(el) {
    if (el._lazyTimer) clearTimeout(el._lazyTimer);
    if ((el as any)._lazyObserver) (el as any)._lazyObserver.disconnect();
  },
};