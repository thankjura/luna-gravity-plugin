// Luna exposes version: 4.0.0-SNAPSHOT
declare module 'luna' {
  import type { ComputedRef, DefineComponent, InjectionKey, Ref, SlotsType } from 'vue';
  import type { LocationQueryRaw, RouteLocationRaw } from 'vue-router';
  import type { AxiosResponse } from 'axios';
  import type { SortableEvent } from 'sortablejs';
  import type { Emitter } from 'mitt';

  export type { Emitter } from 'mitt';

  export const AssignStrategy: { readonly LEAD: "lead"; readonly AUTHOR: "author" };
  export const ColorStrategy: { readonly Priority: "priority"; readonly Query: "queryString" };
  export const ISSUE_PERMS: readonly ["edit", "assign", "transit", "move", "delete", "create_attachments", "create_comments", "create_worklogs", "create_issues", "view_watchers", "manage_watchers", "browse_workflow", "edit_all_comments", "edit_own_comments", "delete_all_comments", "delete_own_comments", "delete_all_attachments", "delete_own_attachments", "edit_all_worklogs", "edit_own_worklogs", "delete_all_worklogs", "delete_own_worklogs"];
  export const IconSize: { readonly ORIGINAL: "original"; readonly SMALL: "32x32"; readonly MEDIUM: "64x64"; readonly LARGE: "128x128"; readonly X_LARGE: "256x256" };
  export const IconType: { readonly ISSUE_TYPE: "issuetypes"; readonly PRIORITY: "priorities"; readonly PROJECT: "projects"; readonly AVATAR: "users" };
  export const SprintState: { readonly NEW: "new"; readonly ACTIVE: "active"; readonly COMPLETE: "complete" };
  export const StatusCategoryKey: { readonly TODO: "todo"; readonly IN_PROGRESS: "in_progress"; readonly DONE: "done" };

  export class BundleI18N implements I18N {
    strings: Ref<Record<string, string>>;
    locale: Ref<string>;
    supportedLocales: Array<string>;
    loader: (locale: string) => Promise<Record<string, string>>;
    constructor(supportedLocales: Array<string>, loader: (locale: string) => Promise<Record<string, string>>);
    loadMessages(locale: string): Promise<void>;
    t(text: string, ...args: Array<string|number>): string;
    p(text: string, params: {gender: 'male'|'female'}, ...args: Array<string|number>): string;
  }

  export const BusyIconComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {},
    {},
    {},
    SlotsType<{}>
  >;
  export const ButtonBusy: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      type?: "button" | "submit" | "reset"
      busy?: boolean
      disabled?: boolean
    },
    {},
    {},
    SlotsType<{
      default: any
    }>
  >;
  export const LoadOverlayComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      absolute?: boolean
      dim?: boolean
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const PagePaginator: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'setPage'(_page: number): void
    },
    'setPage',
    {
      busy?: boolean
      compact?: boolean
      page?: number
      maxItems?: number
      limit?: number
      total: number
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const PopoverComponent: DefineComponent<
    {},
    {
      toggle: (event: MouseEvent, target?: HTMLElement) => void
      show: (event: MouseEvent, target?: HTMLElement) => void
      hide: () => void
      active: boolean
    },
    {},
    {},
    {},
    {},
    {},
    {
      'hide'(): void
    },
    'hide',
    {
      closeButton?: boolean
      prefferedAlign?: ALIGN[]
      group?: string
    },
    {},
    {},
    SlotsType<{
      default: any
    }>
  >;
  export const StatusComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      name?: string
      category?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const TogglePanel: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'toggle'(_state: boolean): void
      'show'(): void
    },
    'toggle' | 'show',
    {
      hideActionsOnCollapse?: boolean
      initState?: boolean
      hideActionsOnHover?: boolean
      title?: string
      description?: string
      storeKey?: string
    },
    {},
    {},
    SlotsType<{
      actions: any
      default: any
    }>
  >;
  export const UserAvatarComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      size?: IconSize
      user: { id?: string | number; displayName?: string; login?: string; iconUrl?: string; }
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const UserLinkComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      showIcon?: boolean
      user?: UserValueDto
      label?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const BaseDialog: DefineComponent<
    {},
    {
      show: () => void
      hide: () => void
    },
    {},
    {},
    {},
    {},
    {},
    {
      'closed'(): void
    },
    'closed',
    {
      busy?: boolean
      showClose?: boolean
    },
    {},
    {},
    SlotsType<{
      header: any
      default: any
      footer: any
    }>
  >;
  export const ButtonsGroupComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      max?: number
      moreLabel?: string
      maxWidth?: number
      zIndex?: number
      group?: string
      options: DropDownGroupOption[]
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const DropDownButton: DefineComponent<
    {},
    {
      show: () => Promise<void>
      hide: () => void
      active: boolean
    },
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      busy?: boolean
      disabled?: boolean
      maxWidth?: number
      zIndex?: number | "auto"
      toggleIcon?: boolean
      fixed?: boolean
      align?: "left" | "right" | "auto"
      cacheOptions?: boolean
      offset?: { x: number; y: number; }
      group?: string
      options: DropDownGroupOption[] | OptionsGetterSync | OptionsGetterAsync
      layerClass?: string
    },
    {},
    {},
    SlotsType<{
      default: any
    }>
  >;
  export const DropDownLayer: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'clicked'(_option: DropDownOption, _event: MouseEvent): void
    },
    'clicked',
    {
      busy?: boolean
      prefferedAlign?: "left" | "right"
      maxWidth?: number
      zIndex?: number
      fixed?: boolean
      align?: "left" | "right" | "auto"
      options?: DropDownGroupOption[]
      parentPosition?: ElemRect
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const DropDownLayerItem: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      option?: DropDownOption
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const CheckBoxesComponent: DefineComponent<
    {},
    {
      focusTerm: () => void
      addValue: (opt: T) => void
      delValue: (optId: string | number) => void
    },
    {},
    {},
    {},
    {},
    {},
    {
      'select'(value: T): void
      'remove'(value: string | number): void
      'update:modelValue'(value: (string | number)[]): void
    },
    'select' | 'remove' | 'update:modelValue',
    {
      optionGroups: OptionsGroup<T>[]
      valueKey?: keyof T
      disabled?: boolean
      busyValue?: string | number
      emitsOnly?: boolean
      maxHeight?: string
      modelValue?: (string | number)[]
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const ColorPickerDialog: DefineComponent<
    {},
    {
      show: (colorValue: string, elementValue: HTMLElement) => void
      colorHex: string
      visible: boolean
    },
    {},
    {},
    {},
    {},
    {},
    {
      'update'(_color: string): void
    },
    'update',
    {
      manualInput?: boolean
      manualInputPlaceholder?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const ColorPickerInput: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: string): void
    },
    'update:modelValue',
    {
      modelValue?: string
    },
    {},
    {},
    SlotsType<{
      default: any
    }>
  >;
  export const ColorPickerLayer: DefineComponent<
    {},
    {
      layer: HTMLDivElement
    },
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: HSV): void
    },
    'update:modelValue',
    {
      modelValue?: HSV
      field?: HTMLElement
      origValue?: HSV
    },
    {},
    {},
    SlotsType<{
      default: any
      footer: any
    }>
  >;
  export const DateTimePicker: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: string): void
    },
    'update:modelValue',
    {
      disabled?: boolean
      withTime?: boolean
      modelValue?: string
      placeholder?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const IconSelector: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: string): void
    },
    'update:modelValue',
    {
      type: IconType
      modelValue?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const MultiSelect: DefineComponent<
    {},
    {
      addValue: (opt: T) => void
      delValue: (optId: T[K]) => void
      showLayer: () => void
      hideLayer: () => void
    },
    {},
    {},
    {},
    {},
    {},
    {
      'select'(value: T): void
      'remove'(value: T[K]): void
      'create'(value: string): void
      'update:modelValue'(value: T[K][]): void
    },
    'select' | 'remove' | 'create' | 'update:modelValue',
    {
      options: T[] | OptionsFunc<T, K>
      modelData?: T[]
      showIcons?: boolean
      minTagLen?: number
      suggestionKey?: keyof T
      tagLabel?: (term: string) => string
      labelKey?: keyof T
      valueKey?: keyof T & K
      id?: string
      cropSuggestions?: boolean
      iconRadius?: string
      missingIconLiteralKey?: keyof T
      modelValue?: T[K][]
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const QsInput: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: string): void
      'update:error'(value: string): void
    },
    'update:modelValue' | 'update:error',
    {
      disabled?: boolean
      maxResult?: number
      modelValue?: string
      error?: string
      initFunc?: () => Promise<QsFormData>
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const QsViewer: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      qs?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SingleSelect: DefineComponent<
    {},
    {
      setValue: (opt: T) => void
      showLayer: () => void
      hideLayer: () => void
    },
    {},
    {},
    {},
    {},
    {},
    {
      'select'(value: T): void
      'create'(value: string): void
      'update:modelValue'(value: T[K]): void
    },
    'select' | 'create' | 'update:modelValue',
    {
      options: T[] | OptionsFunc<T, K>
      modelData?: T
      showIcons?: boolean
      minTagLen?: number
      tagLabel?: (term: string) => string
      suggestionKey?: keyof T
      valueKey?: keyof T & K
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
      modelValue?: T[K]
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SliderTrack: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: number): void
    },
    'update:modelValue',
    {
      max?: number
      min?: number
      step?: number
      modelValue?: number
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SortableList: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update'(value: T[]): void
      'moved'(optionId: T["id"], prevOptionId: T["id"]): void
      'deleted'(option: T): void
      'update:modelValue'(value: T[]): void
    },
    'update' | 'moved' | 'deleted' | 'update:modelValue',
    {
      modelValue: T[]
      showIcons?: boolean
      removable?: boolean
      emitsOnly?: boolean
      busy?: boolean
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SortableSelect: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update'(_value: T[]): void
      'update:modelValue'(value: T[]): void
    },
    'update' | 'update:modelValue',
    {
      showIcons?: boolean
      options: T[] | OptionsFunc<T>
      modelValue?: T[]
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SuggestionIconComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      option: T
      missingIconLiteralKey: keyof T
      iconRadius?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SuggestionsComponent: DefineComponent<
    {},
    {
      focusNext: (event?: KeyboardEvent) => void
      updatePosition: () => void
    },
    {},
    {},
    {},
    {},
    {},
    {
      'hide'(): void
      'select'(value: T): void
      'create'(value: string): void
    },
    'hide' | 'select' | 'create',
    {
      suggestions: T[]
      showIcons?: boolean
      term?: string
      field?: HTMLElement
      tag?: string
      valueKey: keyof T
      suggestionKey?: keyof T | ((item: T) => string)
      cropSuggestions: boolean
      iconRadius?: string
      missingIconLiteralKey?: keyof T
      width?: string
      tagLabel?: (term: string) => string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const MarkdownEditor: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: string): void
    },
    'update:modelValue',
    {
      modelValue?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const MarkdownViewer: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      value?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const ProgressBar: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      busy?: boolean
      total?: number
      value?: number
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const ProgressComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      total: number
      value: number
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const ImageViewer: DefineComponent<
    {},
    {
      show: (imagesData: ImageItem[], idx?: number) => void
    },
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {},
    {},
    {},
    SlotsType<{}>
  >;
  export const CopyToBufferButton: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    string,
    {
      value?: string
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const ToggleComponent: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'update:modelValue'(value: boolean): void
    },
    'update:modelValue',
    {
      busy?: boolean
      disabled?: boolean
      manual?: boolean
      id?: string
      modelValue?: boolean
    },
    {},
    {},
    SlotsType<{}>
  >;
  export const SortableTable: DefineComponent<
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {
      'updated'(_event: SortableEvent): void
      'changed'(_event: SortableEvent): void
    },
    'updated' | 'changed',
    {
      group?: string
    },
    {},
    {},
    SlotsType<{
      header: any
      default: any
      footer: any
    }>
  >;

  export const $i18n: typeof BundleI18N;
  export const $notify: NotifyComponentInterface;
  export const $bus: Emitter<BusEvents>;
  export const RouteNames: { readonly home: "home"; readonly dashboard: "dashboard"; readonly navigator: "navigator"; readonly bulkAction: "bulkAction"; readonly bulkActionProgress: "bulkActionProgress"; readonly projects: "projects"; readonly project: { readonly project: "project"; readonly summary: "projectSummary"; readonly issues: "projectIssues"; readonly activity: "projectActivity"; readonly reports: "projectReports"; readonly settings: { readonly settings: "projectSettings"; readonly summary: "projectSettingsSummary"; readonly details: "projectSettingsDetails"; readonly schemas: "projectSettingsSchemas"; readonly users: "projectSettingsUsers"; readonly permissions: "projectSettingsPermissions"; readonly issueTypes: "projectSettingsIssueTypes"; readonly notifications: "projectSettingsNotifications"; readonly priorities: "projectSettingsPriorities"; readonly indexing: "projectSettingsIndexing"; readonly delete: "projectSettingsDelete" } }; readonly login: "login"; readonly logout: "logout"; readonly profile: "profile"; readonly searchFilters: "searchFilters"; readonly issue: "issue"; readonly agile: { readonly boards: "agileBoards"; readonly view: "agileBoardView"; readonly backlog: "agileBoardBacklog"; readonly reports: "agileBoardReports"; readonly settings: "agileBoardSettings" }; readonly admin: { readonly admin: "admin"; readonly users: { readonly users: "adminUsers"; readonly list: "adminUserList"; readonly user: "adminUser"; readonly roles: "adminUserRoles" }; readonly groups: "adminGroups"; readonly issueTypes: "adminIssueTypes"; readonly issueTypeSchemas: "adminIssueTypeSchemas"; readonly priorities: "adminPriorities"; readonly prioritySchemas: "adminPrioritySchemas"; readonly statuses: "adminStatuses"; readonly workflows: "adminWorkflows"; readonly workflowSchemas: "adminWorkflowSchemas"; readonly projects: "adminProjects"; readonly archivedProjects: "adminArchivedProjects"; readonly projectRoles: "adminProjectRoles"; readonly customFields: "adminCustomFields"; readonly screens: "adminScreens"; readonly screenSchemas: "adminScreenSchemas"; readonly issueTypeScreenSchemas: "adminIssueTypeScreenSchemas"; readonly permissionSchemas: "adminPermissionSchemas"; readonly outgoingMailServers: "adminOutgoingMailServers"; readonly outgoingMailQueue: "adminOutgoingMailQueue"; readonly incomingMailServers: "adminIncomingMailServers"; readonly messageHandlers: "adminMessageHandlers"; readonly notificationSchemas: "adminNotificationSchemas"; readonly themes: "adminThemes"; readonly fieldLayouts: "adminFieldLayouts"; readonly fieldLayoutSchemas: "adminFieldLayoutSchemas"; readonly globalPermissions: "adminGlobalPermissions"; readonly properties: "adminProperties"; readonly systemInfo: "adminSystemInfo"; readonly plugins: "adminPlugins"; readonly scheduler: "adminScheduler"; readonly issueLinkTypes: "adminIssueLinkTypes"; readonly directories: "adminDirectories"; readonly indexing: "adminIndexing"; readonly backup: "adminBackup"; readonly projectImport: "adminProjectImport"; readonly logging: "adminLogging"; readonly ranking: "adminRanking"; readonly docs: "adminDocs"; readonly license: "adminLicense"; readonly icons: "adminIcons"; readonly eventTypes: "adminEventTypes"; readonly mailTester: "adminMailTester"; readonly cluster: "adminCluster" }; readonly maintenance: "maintenance" };
  export const Injections: { issueKey: InjectionKey<Ref<string, string>>; selectedIssues: InjectionKey<ComputedRef<Array<IssueDto>>>; project: InjectionKey<Ref<ProjectInfoDto, ProjectInfoDto>>; board: InjectionKey<Ref<BoardDto, BoardDto>>; workflowAction: InjectionKey<Ref<WorkflowActionDto, WorkflowActionDto>>; workflowFuncType: InjectionKey<Ref<WorkflowFunctionType, WorkflowFunctionType>>; workflowActionFunc: InjectionKey<Ref<WorkflowFunctionDescriptorDto, WorkflowFunctionDescriptorDto>>; };
  export const projectService: ProjectService;
  export const issueTypeService: IssueTypeService;
  export const queryService: QueryService;
  export const fieldService: FieldService;
  export const issueEventService: IssueEventService;

  export interface AttachmentDto {
      id: number,
      author: UserInfoDto,
      created: string,
      fileName: string,
      fileUrl: string,
      fileSize: number,
      mimeType: string,
      thumbName: string,
      thumbUrl: string,
  }
  export interface BaseOption {
      //name: string;
      [key: string]: any;
  }
  export interface BoardColumnDto {
      id: number,
      name: string,
      statusIds: Array<number>,
  }
  export interface BoardDto extends BoardInfoDto {
      filterId: number,
      columns: Array<BoardColumnDto>,
      filters: Array<BoardFilterDto>,
      rankField: IssueFieldInfoDto,
      cardColorStrategy?: ColorStrategy,
      cardColorPreset?: CardColorPresetDto,
      cardExtraFieldShowNames: boolean,
      cardExtraFieldIds: Array<string>,
      cardExtraFields: Array<IssueFieldInfoDto>,
      detailViewFieldIds: Array<string>,
      detailViewFields: Array<IssueFieldInfoDto>,
      doneLimitDays: number,
      activeSprints: Array<SprintInfoDto>,
  }
  export interface BoardFilterDto {
      id: number,
      name: string,
      description: string,
      queryString: string,
  }
  export interface BoardInfoDto extends BoardValueDto, SharedObjectDto {
      description: string,
      author: UserInfoDto,
  }
  export interface BoardValueDto {
      id: number,
      name: string,
      type: BoardType,
  }
  export interface CardColorDto {
      id: number,
      value: string,
      color: string,
  }
  export interface CardColorPresetDto {
      strategy: string,
      colors: Array<CardColorDto>,
  }
  export interface ClauseValue {
      value: string,
      displayValue: string,
  }
  export interface DeleteResult {
      count: number
  }
  export interface DropDownGroupOption {
      id: ID,
      label?: string;
      options: Array<DropDownOption>;
      className?: string,
  }
  export interface DropDownOption {
      id: ID,
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
  export interface ElemRect {
    x: number,
    y: number,
    width: number,
    height: number
  }
  export interface Errors {
      [key: string]: string
  }
  export interface FieldLayoutSchemaDto extends FieldLayoutValueDto {
      description: string,
      defaultLayout: FieldLayoutValueDto,
      entries: Array<FieldLayoutSchemaEntryDto>
  }
  export interface FieldLayoutSchemaEntryDto {
      issueType: IssueTypeValueDto,
      fieldLayout: FieldLayoutValueDto,
  }
  export interface FieldLayoutValueDto {
      id: number,
      name: string,
      description: string,
  }
  export interface FieldSearcherDescriptorDto {
      key: string,
      name: string,
      description?: string,
  }
  export interface FieldService {
    getById: (id: string) => Promise<AxiosResponse<IssueFieldResolvedDto>>
    getCustomFields: (includeBroken: boolean) => Promise<AxiosResponse<Array<IssueFieldInfoTypeDto>>>
    getStatisticFields: () => Promise<AxiosResponse<Array<IssueFieldInfoDto>>>
    getRankFields: () => Promise<AxiosResponse<Array<IssueFieldInfoDto>>>
    getBulkEditableFields: () => Promise<AxiosResponse<Array<IssueFieldInfoDto>>>
    getSystemFields: () => Promise<AxiosResponse<Array<IssueFieldInfoDto>>>
    getAllFields: (includeBroken: boolean, force?: boolean) => Promise<AxiosResponse<Array<IssueFieldInfoTypeDto>>>
    createCustomField: (name: string | null, description: string | null, fieldType: string | null) => Promise<AxiosResponse<IssueFieldDto>>
    updateCustomField: (id: string, name: string | null, description?: string | null, searcher?: string | null) => Promise<AxiosResponse<IssueFieldResolvedDto>>
    updateFieldRenders: (id: string, renders: Partial<IssueFieldDto>) => Promise<AxiosResponse<IssueFieldResolvedDto>>
    deleteCustomField: (id: string) => Promise<AxiosResponse<DeleteResult>>
    getFieldTypes: () => Promise<AxiosResponse<Array<FieldTypeDescriptorDto>>>
    getFieldSearchers: (fieldId: string) => Promise<AxiosResponse<Array<FieldSearcherDescriptorDto>>>
    createFieldContext: (fieldId: string, context: Partial<IssueFieldContextDto>) => Promise<AxiosResponse<IssueFieldResolvedDto>>
    updateFieldContext: (fieldId: string, contextId: number, context: Partial<IssueFieldContextDto>) => Promise<AxiosResponse<IssueFieldResolvedDto>>
    deleteFieldContext: (fieldId: string, contextId: number) => Promise<AxiosResponse<IssueFieldResolvedDto>>
    getOptions: (fieldId: string, contextId: number) => Promise<AxiosResponse<Array<Option<number>>>>
    createOption: (fieldId: string, contextId: number, option: Partial<Option<number>>) => Promise<AxiosResponse<Option<number>>>
    updateOption: (fieldId: string, contextId: number, optionId: string, option: Partial<Option<number>>) => Promise<AxiosResponse<Option<number>>>
    deleteOption: (fieldId: string, contextId: number, optionId: number) => Promise<AxiosResponse<DeleteResult>>
    moveOption: (fieldId: string, contextId: number, optionId: number, prevOptionId: number | null) => Promise<AxiosResponse<Array<Option<number>>>>
  }
  export interface FieldTypeDescriptorDto {
      key: string,
      name: string,
      description?: string,
      iconUrl?: string,
      navigatorComponents?: Array<string>,
      viewComponents?: Array<string>,
      formComponents?: Array<string>,
  }
  export interface I18N {
      locale: Ref<string>,
      loadMessages(locale: string): Promise<void>,
      supportedLocales: Array<string>,
      t(text: string, ...args: Array<string|number>): string,
      p(text: string, params: Record<string, any>, ...args: Array<string|number>): string,
  }
  export interface ImageItem {
    id: number,
    name: string,
    description?: string,
    imageUrl: string,
    thumbUrl?: string,
  }
  export interface IssueDto {
      id: number,
      key: string,
      summary?: string,
      issueType?: IssueTypeValueDto,
      project?: ProjectValueDto,
      parent?: IssueValueDto,
      assignee?: UserValueDto,
      author?: UserValueDto,
      priority?: PriorityValueDto,
      estimate?: string,
      timeLeft?: string,
      timeSpent?: string,
      estimateValue?: number,
      timeLeftValue?: number,
      timeSpentValue?: number,
      status?: StatusValueDto,
      statusCategory?: StatusCategory,
      description?: string,
      created?: string,
      updated?: string,
      sprint?: SprintValueDto,
      rank?: string,
      labels?: Array<string>,
      watchers?: WatchersInfo,
      perms?: Array<IssuePermKey>,
      color?: string,
      [customField: `f${number}`]: FieldValue
  }
  export interface IssueEventService {
    getAll: (force?: boolean) => Promise<AxiosResponse<Array<IssueEventType>>>;
    create: (name: string, description: string, baseEventTypeId: number) => Promise<AxiosResponse<IssueEventType>>;
    update: (eventTypeId: number, name: string, description: string, baseEventTypeId: number) => Promise<AxiosResponse<IssueEventType>>;
    delete: (eventTypeId: number) => Promise<AxiosResponse<DeleteResult>>;
  }
  export interface IssueEventType {
      id: number,
      name: string,
      description: string | null,
      baseEventTypeId: number,
      systemEvent: boolean,
  }
  export interface IssueFieldContextDto {
      id: number,
      name: string,
      description: string,
      projectIds: Array<number>,
      issueTypeIds: Array<number>,
      options: Array<Option<number>>
  }
  export interface IssueFieldContextResolvedDto extends IssueFieldContextDto {
      projects: Array<ProjectInfoDto>,
      issueTypes: Array<IssueTypeDto>,
      defaultViewValue?: FieldValue,
      defaultFormValue?: FieldValue,
      formParams: any,
  }
  export interface IssueFieldDto extends IssueFieldInfoTypeDto {
      formComponents?: Array<string>,
      viewComponents?: Array<string>,
      navigatorComponents?: Array<string>,
      optionsComponent?: string,
      searcherKey: string
      searcherName: string,
      valid: boolean,
  }
  export interface IssueFieldInfoDto {
      id: keyof IssueDto,
      name: string,
      description: string,
      systemField: boolean,
      viewComponent?: string,
      navigatorComponent?: string,
      formComponent?: string,
  }
  export interface IssueFieldInfoTypeDto extends IssueFieldInfoDto {
      fieldTypeKey: string,
      fieldTypeName: string,
      searcherName: string,
      valid: boolean,
  }
  export interface IssueFieldResolvedDto extends IssueFieldDto {
      contexts: Array<IssueFieldContextResolvedDto>,
  }
  export interface IssueTypeDto extends IssueTypeValueDto {
      iconPath: string,
      description?: string
  }
  export interface IssueTypeSchemaDto extends IssueTypeScreenSchemaValueDto {
      description?: string,
      issueTypes: Array<IssueTypeDto>,
  }
  export interface IssueTypeScreenSchemaDto extends IssueTypeScreenSchemaValueDto {
      description?: string | null,
      defaultScreenSchema: ScreenSchemaValueDto,
      entries: {
          [key: number]: ScreenSchemaValueDto
      },
      assignedIssueTypesMap: {[key: number]: IssueTypeValueDto}
  }
  export interface IssueTypeScreenSchemaValueDto {
      id: number,
      name: string,
  }
  export interface IssueTypeService {
    create: (name: string | null, description: string | null, subtask: boolean, iconPath?: string | null) => Promise<AxiosResponse<IssueTypeDto>>
    update: (id: number, name: string | null, description?: string | null, iconPath?: string | null) => Promise<AxiosResponse<IssueTypeDto>>
    getById: (id: number) => Promise<AxiosResponse<IssueTypeDto>>
    search: (params: SearchQuery, excludes?: Array<number>) => Promise<AxiosResponse<SearchResult<IssueTypeDto>>>
    getAll: () => Promise<AxiosResponse<Array<IssueTypeDto>>>
    delete: (id: number) => Promise<AxiosResponse<DeleteResult>>
    getSchema: (id: number) => Promise<AxiosResponse<IssueTypeSchemaDto>>
    searchSchemas: (params: SearchQuery, excludes?: Array<number>) => Promise<AxiosResponse<SearchResult<IssueTypeSchemaDto>>>
    createSchema: (name?: string | null, description?: string | null, issueTypes?: Array<number>) => Promise<AxiosResponse<IssueTypeSchemaDto>>
    updateSchema: (id: number, name?: string | null, description?: string | null, issueTypes?: Array<number>) => Promise<AxiosResponse<IssueTypeSchemaDto>>
    deleteSchema: (id: number) => Promise<AxiosResponse<DeleteResult>>
  }
  export interface IssueTypeValueDto {
      id: number,
      name: string,
      subtask?: boolean,
      iconUrl: string,
  }
  export interface IssueValueDto {
      id: number,
      key: string,
      summary: string,
      iconTypeUrl: string,
  }
  export interface NotificationSchemaValueDto {
      id: number,
      name: string,
      description: string|null,
  }
  export interface NotifyComponentInterface {
      info: (title: string, body?: string, closable?: boolean) => void;
      warn: (title: string, body?: string, closable?: boolean) => void;
      ok: (title: string, body?: string, closable?: boolean) => void;
      error: (title: string, body?: string, closable?: boolean) => void;
  }
  export interface Option<T extends ID> {
      id: T,
      name: string,
      iconUrl?: string,
      altName?: string,
  }
  export interface OptionsGroup<T extends BaseOption> {
      id: ID,
      label?: string,
      options: Array<T>
  }
  export interface ParseResult {
      valid: boolean,
      errorMessage: string | null,
      errors: Array<QueryParseError>
  }
  export interface PermissionSchemaValueDto {
      id: number,
      name: string,
      description: string|null,
  }
  export interface PriorityDto extends PriorityValueDto {
      iconPath?: string,
      position: number
  }
  export interface PrioritySchemaDto extends PrioritySchemaValueDto {
      description?: string,
      priorities: Array<PriorityDto>,
  }
  export interface PrioritySchemaValueDto {
      id: number,
      name: string,
  }
  export interface PriorityValueDto {
      id: number,
      name: string,
      description: string,
      iconUrl: string,
  }
  export interface ProjectDto extends ProjectInfoDto {
      lead: UserValueDto,
      created: string,
      iconPath?: string,
      assignStrategy: AssignStrategy,
      canAdmin: boolean,
  }
  export interface ProjectInfoDto extends ProjectValueDto {
      description?: string,
      archived?: boolean,
      archivedDate?: string,
  }
  export interface ProjectMembersDto {
      projectRoles: Array<ProjectRole>,
      usersMap: {
          [key: number]: Array<UserInfoDto>
      },
      groupsMap: {
          [key: number]: Array<string>
      },
  }
  export interface ProjectRole {
      id: number,
      name: string,
      description?: string
  }
  export interface ProjectSchemasIds {
      issueTypeSchemaId: number | null,
      resolutionSchemaId: number | null,
      issueTypeScreenSchemaId: number | null,
      fieldLayoutSchemaId: number | null,
      prioritySchemaId: number | null,
      workflowSchemaId: number|null,
      permissionSchemaId: number|null,
      notificationSchemaId: number|null,
  }
  export interface ProjectSearchQuery extends SearchQuery {
      state?: 'active' | 'archived' | 'any'
  }
  export interface ProjectService {
    create: (key: string | null, name: string | null, description?: string | null, iconPath?: string | null) => Promise<AxiosResponse<ProjectWithSchemas>>
    patch: (key: string, data: Partial<ProjectWithSchemaIdsDto> & {leadId?: number}) => Promise<AxiosResponse<ProjectWithSchemas>>
    get: (keyOrId: string|number) => Promise<AxiosResponse<ProjectDto>>
    getWithSchemas: (key: string) => Promise<AxiosResponse<ProjectWithSchemas>>
    getAll: () => Promise<AxiosResponse<Array<ProjectValueDto>>>
    search: (params: ProjectSearchQuery, excludes?: Array<string|number>, isAdmin?: boolean) => Promise<AxiosResponse<SearchResult<ProjectInfoDto>>>
    delete: (key: string) => Promise<AxiosResponse<DeleteResult>>
    getMembers: (key: string) => Promise<AxiosResponse<ProjectMembersDto>>
    addMembers: (key: string, roles: Array<number>, groups: Array<string>, users: Array<string>) => Promise<AxiosResponse<ProjectMembersDto>>
    removeUser: (key: string, role: number, userName: string) => Promise<AxiosResponse<ProjectMembersDto>>
    removeGroup: (key: string, role: number, groupName: string) => Promise<AxiosResponse<ProjectMembersDto>>
    removeRole: (key: string, role: number) => Promise<AxiosResponse<ProjectMembersDto>>
  }
  export interface ProjectValueDto {
      id: number,
      key: string,
      name: string,
      iconUrl: string,
  }
  export interface ProjectWithSchemaIdsDto extends ProjectDto, ProjectSchemasIds {}
  export interface ProjectWithSchemas extends ProjectDto {
      fieldLayoutSchema: FieldLayoutSchemaDto | null,
      issueTypeSchema: IssueTypeSchemaDto | null,
      issueTypeScreenSchema: IssueTypeScreenSchemaDto | null,
      prioritySchema: PrioritySchemaDto | null,
      workflowSchema: WorkflowSchemaDto | null,
      permissionSchema: PermissionSchemaValueDto | null,
      notificationSchema: NotificationSchemaValueDto | null,
  }
  export interface QsFormData {
      searchers: Array<QuerySearcherDto>,
      sorters: Array<QuerySorterDto>,
      functions: Array<QueryFunctionDto>,
  }
  export interface QueryFunctionDto {
      functionName: string,
      value: string,
      list: boolean,
      valueType: string
  }
  export interface QueryParseError {
      line: number,
      position: number,
      message: string
  }
  export interface QueryRepresentation {
      valid: boolean,
      searchers: Array<SearcherRepresentation>,
      sorters: Array<SortRepresentationDto>,
  }
  export interface QuerySearcherDto {
      id: string,
      name: string,
      nameQs: string,
      valueType: string,
      allowedOperators: Array<QueryOperator>,
      supportBasicSearch: boolean,
  }
  export interface QueryService {
    parseQuery: (qs: string) => Promise<AxiosResponse<ParseResult>>
    getQsData: () => Promise<AxiosResponse<QsFormData>>
    getSuggestions: (fieldName: string, term: string | null, excludes: Array<string>) => Promise<AxiosResponse<Array<ClauseValue>>>
    getSuggestionsForType: (valueType: string, term: string | null, excludes: Array<string>) => Promise<AxiosResponse<Array<ClauseValue>>>
    transformQuery: (qs: string) => Promise<AxiosResponse<QueryRepresentation>>
    transformSearcher: (fieldId: string, params: any) => Promise<AxiosResponse<SearcherRepresentation>>
  }
  export interface QuerySorterDto {
      id: string,
      name: string,
      label: string,
  }
  export interface ResponseError {
      status: number,
      reason: string,
      errors: Errors
  }
  export interface ScreenSchemaValueDto {
      id: number,
      name: string,
  }
  export interface ScreenValueDto {
      id: number,
      name: string,
      description: string,
  }
  export interface SearchFilterValueDto {
      id: number,
      name: string,
      queryString: string,
  }
  export interface SearchQuery extends LocationQueryRaw {
      term?: string,
      page?: number,
      limit?: number,
  }
  export interface SearchResult<T> {
      total: number,
      results: Array<T>,
      page: number,
      limit: number,
  }
  export interface SearcherRepresentation {
      id: string,
      name: string,
      qsFragment: string,
      viewLabel: string,
      formComponent: string,
      formValue: any,
  }
  export interface ShareEntryDto  {
      users: Array<UserValueDto>;
      userIds: Array<number>;
      groupNames: Array<string>;
  }
  export interface SharedObjectDto {
      editAccess: boolean,
      viewAccess: boolean,
      viewPerms: ShareEntryDto,
      editPerms: ShareEntryDto,
  }
  export interface SortRepresentationDto {
      id: string,
      name: string,
      asc: boolean,
      label: string,
  }
  export interface SprintInfoDto extends SprintValueDto {
      goal: string,
      startDate: string,
      displayName: string,
      finishDate: string,
      boardId: number,
  }
  export interface SprintValueDto {
      id: number,
      name: string,
      state: SprintState,
  }
  export interface StatusCategory {
      key: StatusCategoryKey,
      name: string,
  }
  export interface StatusValueDto {
      id: number,
      name: string,
      categoryKey: StatusCategoryKey,
  }
  export interface UserActivityDto {
      navigatorQs: string | null,
      navigatorFilterId: number | null,
      lastVisitedIssuesIds: Array<number>,
      lastVisitedProjectIds: Array<number>,
      navigatorColumns: Array<IssueFieldInfoDto>,
      lastVisitedFilters: Array<SearchFilterValueDto>,
      lastVisitedBoardIds: Array<number>,
  }
  export interface UserFavorites {
      userId: number,
      filterIds: Array<number>,
      boardIds: Array<number>,
  }
  export interface UserInfoDto extends UserValueDto {
      gender: Gender,
  }
  export interface UserProfileDto extends UserInfoDto {
      activity: UserActivityDto,
      favorites: UserFavorites,
      admin: boolean,
      name: string,
      lastName: string,
      locale: string,
      dashboardId: number,
      external: boolean,
  }
  export interface UserValueDto {
      id: number,
      login: string,
      displayName: string,
      email: string,
      iconUrl: string,
      active: boolean,
      name: string,
  }
  export interface WatchersInfo {
      count: number,
  }
  export interface WorkflowActionConditionGroup {
      id: string,
      condition: WorkflowActionFunction | null,
      items: Array<WorkflowActionConditionGroup> | null,
      operator: ConditionOperator,
  }
  export interface WorkflowActionDto extends WorkflowActionValueDto {
      sourceStatusIds: Array<number>,
      targetStatusId: number,
      screen: ScreenValueDto|null,
      validators: Array<WorkflowActionFunction>,
      conditions: WorkflowActionConditionGroup,
      postfunctions: Array<WorkflowActionFunction>,
  }
  export interface WorkflowActionFunction extends WorkflowFunctionDescriptorDto {
      id: string,
      params: null | WorkflowFunctionParams,
      viewParams: null | WorkflowFunctionViewParams,
  }
  export interface WorkflowActionValueDto {
      id: number,
      name: string,
      screenId: number|null,
  }
  export interface WorkflowFunctionDescriptorDto {
      name: string,
      description: string,
      className: string,
      allowMultiple: boolean,
      viewComponent: string | null,
      formComponent: string | null,
  }
  export interface WorkflowFunctionParams {
      [key: string]: string
  }
  export interface WorkflowFunctionViewParams {
      [key: string]: any
  }
  export interface WorkflowSchemaDto extends WorkflowSchemaValueDto {
      defaultWorkflow: WorkflowValueDto,
      entries: Array<WorkflowSchemaEntryDto>,
      active: boolean,
  }
  export interface WorkflowSchemaEntryDto {
      issueType: IssueTypeValueDto,
      workflow: WorkflowValueDto,
  }
  export interface WorkflowSchemaValueDto {
      id: number,
      name: string,
      description: string|null,
      workflowIds: Array<number>,
  }
  export interface WorkflowValueDto {
      id: number,
      name: string,
      description: string|null,
      originalId: number|null,
  }
  export interface WorklogDto {
      id: number,
      author: UserValueDto,
      created: string
      updater: UserValueDto|null,
      updated: string|null,
      startDate: string,
      timeSpent: string,
      comment: string|null,
  }
  export type ALIGN = 'top' | 'left' | 'bottom' | 'right';
  export type AssignStrategy = typeof AssignStrategy[keyof typeof AssignStrategy];
  export type BoardType = 'kanban'|'scrum';
  export type BusEvents = {
      'issue.updated': {issue: IssueDto, fields?: Array<string>, initiator?: string},
      'issue.created': {issue: IssueDto, initiator?: string},
      'issue.deleted': {issue: IssueDto},
      'issue.worklog.created': {worklog: WorklogDto},
      'issue.worklog.updated': {worklog: WorklogDto},
      'issue.attachment.added': {issueKey: string, attachments: Array<AttachmentDto>, initiator: string},
      'action.create_comment': {issue: IssueDto},
      'action.create_link': {issueKey: string, initiator?: string},
      'action.move_issue': {issue: IssueDto, initiator?: string},
      'action.edit_issue': {issueKey: string, fields?: Array<string>, initiator?: string},
      'action.assign_issue': {issue: IssueDto, fields?: Array<string>, initiator?: string},
      'action.delete_issue': {issue: IssueDto, initiator?: string},
      'action.transit_issue': {issueKey: string, actionId: number, showDialog: boolean, fields?: Array<string>, initiator?: string, cb?: (issue: IssueDto) => void},
      'action.edit_worklog': {issueKey: string, worklog: WorklogDto},
      'action.create_worklog': {issueKey: string},
      'action.edit_field': {issueKey: string, fieldId: string},
      'action.custom': {name: string, issue?: IssueDto, initiator?: string, params: Record<string, string>},
      // workflow
      'workflow.edit_condition': {condGroup: WorkflowActionConditionGroup},
      'workflow.remove_condition': {condGroup: WorkflowActionConditionGroup},
      'workflow.add_condition': {condGroup: WorkflowActionConditionGroup},
      // user
      'user.login.required': {resolve?: (user: UserProfileDto) => void, reject?: () => void},
      'user.self.updated': {user: UserProfileDto},
      // size
      'site.theme.switched': {theme: 'dark'|'light'}

      // project
      'project.updated': {project: ProjectWithSchemas},

      // filters
      'search_filter.deleted': {searchFilterId: number}

      // popovers
      'popover.show': {event: MouseEvent, group: string, target?: Element, id: string},

      // panels
      'panel.event': {event: MouseEvent, name: string, panelId: string, actionId: string},

      // users
      'user.panel.show': {loginOrId: string|number},
  }
  export type ColorStrategy = typeof ColorStrategy[keyof typeof ColorStrategy];
  export type ConditionOperator = "and" | "or";
  export type FieldValue = string | null | undefined | object | Array<string>;
  export type Gender = 'male' | 'female';
  export type HSV = { h: number, s: number, v: number, a?: number };
  export type ID = string|number;
  export type IconSize = typeof IconSize[keyof typeof IconSize];
  export type IconType = typeof IconType[keyof typeof IconType];
  export type IssuePermKey = typeof ISSUE_PERMS[number];
  export type OptionsFunc<T extends BaseOption, K extends keyof T = 'id'> = (term: string | null, selected: Array<T[K]>) => Promise<Array<T>>;
  export type OptionsGetterAsync = () => Promise<DropDownGroupOption[]>
  export type OptionsGetterSync = () => DropDownGroupOption[]
  export type QueryOperator = '=' | '!=' | '~' | '!~' | 'is' | 'not is' | '>' | '>=' | '<' | '<=';
  export type SprintState = typeof SprintState[keyof typeof SprintState];
  export type StatusCategoryKey = typeof StatusCategoryKey[keyof typeof StatusCategoryKey];
  export type WorkflowFunctionType = 'postfunction' | 'validator' | 'condition';

  type K = any;
  type T = any;
}
