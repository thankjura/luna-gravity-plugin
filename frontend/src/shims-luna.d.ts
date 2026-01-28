// Luna exposes version: 1.0.0
declare module 'luna' {
  import type { DefineComponent, AllowedComponentProps, ComponentCustomProps, VNodeProps } from 'vue'
  import RouteLocationRaw from 'vue-router'

  export const BusyIconComponent: DefineComponent<
    {}, {}, {}, {}, {}, {}, {}, {}>
  export const ButtonBusy: DefineComponent<
    {
      busy?: boolean
      disabled?: boolean
    }, {}, {}, {}, {}, {}, {}, {}> & { $slots: {
      'default'(props: {}): any
    }
  };
  export const LoadOverlayComponent: DefineComponent<
    {
      absolute?: boolean
      dim?: boolean
    }, {}, {}, {}, {}, {}, {}, {}>
  export const PagePaginator: DefineComponent<
    {
      busy?: boolean
      compact?: boolean
      page?: number
      maxItems?: number
      limit?: number
      total: number
    }, {}, {}, {}, {}, {}, {}, {
      'setPage': [page: number]
    }>
  export const PopoverComponent: DefineComponent<
    {
      closeButton?: boolean
      prefferedAlign?: ALIGN[]
      group?: string
    }, {
      toggle: (event: MouseEvent, target?: HTMLElement) => void
    
      show: (event: MouseEvent, target?: HTMLElement) => void
    
      hide: () => void
    
      active: boolean
    }, {}, {}, {}, {}, {}, {
      'hide': []
    }> & { $slots: {
      'default'(props: {}): any
    }
  };
  export const StatusComponent: DefineComponent<
    {
      name?: string
      category?: string
    }, {}, {}, {}, {}, {}, {}, {}>
  export const TogglePanel: DefineComponent<
    {
      hideActions?: boolean
      initState?: boolean
      title?: string
      storeKey?: string
    }, {}, {}, {}, {}, {}, {}, {
      'toggle': [state: boolean]
    
      'show': []
    }> & { $slots: {
      'actions'(props: {}): any
      'default'(props: {}): any
    }
  };
  export const UserAvatarComponent: DefineComponent<
    {
      user: Partial<User>
    }, {}, {}, {}, {}, {}, {}, {}>
  export const UserLinkComponent: DefineComponent<
    {
      user?: User
    }, {}, {}, {}, {}, {}, {}, {}>
  export const BaseDialog: DefineComponent<
    {
      busy?: boolean
      showClose?: boolean
    }, {
      show: () => void
    
      hide: () => void
    }, {}, {}, {}, {}, {}, {
      'closed': []
    }> & { $slots: {
      'header'(props: {}): any
      'default'(props: {}): any
      'footer'(props: {}): any
    }
  };
  export const ButtonsGroupComponent: DefineComponent<
    {
      max?: number
      moreLabel?: string
      maxWidth?: number
      zIndex?: number
      group?: string
      options: DropDownOption[]
    }, {}, {}, {}, {}, {}, {}, {}>
  export const DropDownButton: DefineComponent<
    {
      fixed?: boolean
      busy?: boolean
      disabled?: boolean
      maxWidth?: number
      zIndex?: number | "auto"
      toggleIcon?: boolean
      align?: "left" | "right" | "auto"
      cacheOptions?: boolean
      offset?: { x: number; y: number; }
      group?: string
      options: DropDownGroupOption[] | OptionsGetterSync | OptionsGetterAsync
      layerClass?: string
    }, {
      show: () => Promise<void>
    
      hide: () => void
    
      active: boolean
    }, {}, {}, {}, {}, {}, {}> & { $slots: {
      'default'(props: {}): any
    }
  };
  export const DropDownLayer: DefineComponent<
    {
      fixed?: boolean
      prefferedAlign?: "left" | "right"
      maxWidth?: number
      zIndex?: number
      align?: "left" | "right" | "auto"
      options?: DropDownGroupOption[]
      parentPosition?: { x: number; y: number; width: number; height: number; }
    }, {}, {}, {}, {}, {}, {}, {
      'clicked': [option: DropDownOption, event: MouseEvent]
    }>
  export const DropDownLayerItem: DefineComponent<
    {
      option?: DropDownOption
    }, {}, {}, {}, {}, {}, {}, {}>
  export const CheckboxesComponent: DefineComponent<
    {
      modelValue: string[]
      optionGroups: OptionsGroup<T>[]
    }, {
      focusTerm: () => void
    
      addValue: (opt: T) => void
    
      delValue: (optId: string) => void
    }, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: string[]]
    
      'select': [value: T]
    
      'remove': [value: string]
    }>
  export const ColorPickerDialog: DefineComponent<
    {}, {
      show: (colorValue: string, elementValue: HTMLElement) => void
    }, {}, {}, {}, {}, {}, {
      'update': [color: string]
    }>
  export const ColorPickerInput: DefineComponent<
    {
      modelValue?: string
    }, {
      visible: boolean
    
      origColor: { h: number; s: number; v: number; a?: number; }
    
      color: { h: number; s: number; v: number; a?: number; }
    
      value: any
    }, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: Color]
    }>
  export const ColorPickerLayer: DefineComponent<
    {
      modelValue?: HSV
      field?: HTMLElement
      origValue?: HSV
    }, {
      styles: { top?: string; left?: string; }
    
      origColor: { h: number; s: number; v: number; a?: number; }
    
      color: { h: number; s: number; v: number; a?: number; }
    
      mousePressed: boolean
    
      cursorPosition: { top: string; left: string; }
    
      value: any
    
      origColorHex: string
    
      colorHex: string
    }, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: HSV]
    
      'close': []
    }>
  export const DateTimePicker: DefineComponent<
    {
      disabled?: boolean
      withTime?: boolean
      modelValue?: string
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: string]
    }>
  export const IconSelector: DefineComponent<
    {
      type: IconType
      modelValue?: string
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: string]
    }>
  export const MultiSelect: DefineComponent<
    {
      options: T[] | OptionsFunc<T>
      modelData?: T[]
      showIcons?: boolean
      minTagLen?: number
      suggestionKey?: keyof T
      labelKey?: keyof T
      id?: string
      cropSuggestions?: boolean
      iconRadius?: string
      missingIconLiteralKey?: keyof T
      modelValue?: string[]
    }, {
      addValue: (opt: T) => void
    
      delValue: (optId: string) => void
    
      showLayer: () => void
    
      hideLayer: () => void
    }, {}, {}, {}, {}, {}, {
      'select': [value: T]
    
      'remove': [value: string]
    
      'create': [value: string]
    
      'update:modelValue': [value: string[]]
    }>
  export const QsInput: DefineComponent<
    {
      disabled?: boolean
      modelValue?: string
      error?: string
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: string]
    
      'update:error': [value: string]
    
      'submit': []
    
      'heightChanged': []
    }>
  export const QsViewer: DefineComponent<
    {
      qs?: string
    }, {}, {}, {}, {}, {}, {}, {}>
  export const SingleSelect: DefineComponent<
    {
      options: T[] | OptionsFunc<T>
      modelData?: T
      showIcons?: boolean
      minTagLen?: number
      suggestionKey?: keyof T
      labelKey?: keyof T | ((item: T) => string)
      name?: string
      cropSuggestions?: boolean
      disabled?: boolean
      iconRadius?: string
      missingIconLiteralKey?: keyof T
      placeholder?: string
      nullValueName?: string
      enableClear?: boolean
      suggestionsWidth?: string
      id?: string
      modelValue?: string | number
    }, {
      setValue: (opt: T) => void
    
      showLayer: () => void
    
      hideLayer: () => void
    }, {}, {}, {}, {}, {}, {
      'select': [value: T]
    
      'create': [value: string]
    
      'update:modelValue': [value: string | number]
    }>
  export const SliderTrack: DefineComponent<
    {
      max?: number
      min?: number
      step?: number
      modelValue?: number
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: number]
    }>
  export const SortableList: DefineComponent<
    {
      modelValue: T[]
      showIcons?: boolean
      removable?: boolean
      emitsOnly?: boolean
      busy?: boolean
    }, {}, {}, {}, {}, {}, {}, {
      'update': [value: T[]]
    
      'moved': [optionId: string, prevOptionId: string]
    
      'deleted': [option: T]
    
      'update:modelValue': [value: T[]]
    }>
  export const SortableSelect: DefineComponent<
    {
      modelValue: T[]
      showIcons?: boolean
      options: T[] | OptionsFunc<T>
    }, {}, {}, {}, {}, {}, {}, {
      'update': [value: T[]]
    
      'update:modelValue': [value: T[]]
    }>
  export const SuggestionIconComponent: DefineComponent<
    {
      option: T
      missingIconLiteralKey: keyof T
      iconRadius?: string
    }, {}, {}, {}, {}, {}, {}, {}>
  export const SuggestionsComponent: DefineComponent<
    {
      suggestions: T[]
      showIcons?: boolean
      term?: string
      field?: HTMLElement
      tag: string
      suggestionKey?: keyof T | ((item: T) => string)
      cropSuggestions: boolean
      iconRadius?: string
      missingIconLiteralKey?: keyof T
      width?: string
    }, {
      focusNext: (event?: KeyboardEvent) => void
    
      updatePosition: () => void
    }, {}, {}, {}, {}, {}, {
      'hide': []
    
      'select': [value: T]
    
      'create': [value: string]
    }>
  export const MarkdownEditor: DefineComponent<
    {
      modelValue?: string
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: string]
    }>
  export const MarkdownViewer: DefineComponent<
    {
      value?: string
    }, {}, {}, {}, {}, {}, {}, {}>
  export const ProgressBar: DefineComponent<
    {
      busy?: boolean
      value?: number
      total?: number
    }, {
      progress: number
    }, {}, {}, {}, {}, {}, {}>
  export const ProgressComponent: DefineComponent<
    {
      value: number
      total: number
    }, {}, {}, {}, {}, {}, {}, {}>
export const I18N: I18N & {new(supportedLocales: Array<string>, loader: (locale: string) => Promise<Record<string, string>>): I18N}
  export interface NotifyComponentInterface {
      info: (title: string, body?: string, closable?: boolean) => void;
      warn: (title: string, body?: string, closable?: boolean) => void;
      ok: (title: string, body?: string, closable?: boolean) => void;
      error: (title: string, body?: string, closable?: boolean) => void;
  }
  export interface I18N {
      loadMessages(locale: string): void,
      supportedLocales: Array<string>,
      t(text: string, ...args: Array<string|number>): string,
      p(text: string, params: Record<string, any>, ...args: Array<string|number>): string,
  }
  export interface Errors {
      [key: string]: string
  }
  export type ALIGN = 'top' | 'left' | 'bottom' | 'right';
  export interface User {
      id: string
      directoryId: string,
      externalId: string,
      login: string
      email: string
      name: string
      lastName?: string | null
      displayName: string | null
      groups?: Array<string>,
      locale: string,
      iconName?: string,
      iconUrl?: string,
      gender: 'male'|'female',
  }
  export interface DropDownOption {
    id: string,
    label: string;
    iconUrl?: string;
    iconTitle?: string;
    iconName?: string;
    route?: RouteLocationRaw,
    href?: string;
    cb?: (option: DropDownOption, event: MouseEvent) => boolean|undefined|void;
    children?: Array<DropDownGroupOption>;
    childrenLayerClass?: string;
    className?: string,
    title?: string,
    selected?: boolean,
  }
  export interface DropDownGroupOption {
    id: string,
    label?: string;
    options: Array<DropDownOption>;
    className?: string,
  }
  export type OptionsGetterSync = () => DropDownGroupOption[]
  export type OptionsGetterAsync = () => Promise<DropDownGroupOption[]>
  export interface OptionsGroup<T extends Option> {
      id: string,
      label: string,
      options: Array<T>
  }
  export interface Option {
      id: string,
      name: string,
      iconUrl?: string,
      altName?: string,
  }
  T = any
  export type Color = string | HSV | RGB;
  export type HSV = { h: number, s: number, v: number, a?: number };
  export type RGB = { r: number, g: number, b: number, a?: number };
  export enum IconType {
      IssueType = "issuetypes",
      Priority = "priorities",
      Project = "projects",
      Status = "statuses",
  }
  export type OptionsFunc<T extends Option> = (term: string | null, selected: Array<string>) => Promise<Array<T>>;
}

