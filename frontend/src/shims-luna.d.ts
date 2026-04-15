// Luna exposes version: 3.0.0-SNAPSHOT
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
      'setPage': [_page: number]
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
      'toggle': [_state: boolean]
    
      'show': []
    }> & { $slots: {
      'actions'(props: {}): any
      'default'(props: {}): any
    }
  };
  export const UserAvatarComponent: DefineComponent<
    {
      user: { id?: string | number; displayName?: string; login?: string; iconUrl?: string; }
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
      busy?: boolean
      prefferedAlign?: "left" | "right"
      maxWidth?: number
      zIndex?: number
      align?: "left" | "right" | "auto"
      options?: DropDownGroupOption[]
      parentPosition?: { x: number; y: number; width: number; height: number; }
    }, {}, {}, {}, {}, {}, {}, {
      'clicked': [_option: DropDownOption, _event: MouseEvent]
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
    
      delValue: (optId: string | number) => void
    }, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: (string | number)[]]
    
      'select': [value: T]
    
      'remove': [value: string | number]
    }>
  export const ColorPickerDialog: DefineComponent<
    {}, {
      show: (colorValue: string, elementValue: HTMLElement) => void
    }, {}, {}, {}, {}, {}, {
      'update': [_color: string]
    }>
  export const ColorPickerInput: DefineComponent<
    {
      modelValue?: string
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: string]
    }> & { $slots: {
      'default'(props: {}): any
    }
  };
  export const ColorPickerLayer: DefineComponent<
    {
      modelValue?: HSV
      field?: HTMLElement
      origValue?: HSV
    }, {}, {}, {}, {}, {}, {}, {
      'update:modelValue': [value: HSV]
    
      'close': []
    
      'updated': [_hsv: HSV]
    }> & { $slots: {
      'default'(props: {}): any
      'footer'(props: {}): any
    }
  };
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
      valueKey?: keyof T
      id?: string
      cropSuggestions?: boolean
      iconRadius?: string
      missingIconLiteralKey?: keyof T
      modelValue?: (string | number)[]
    }, {
      addValue: (opt: T) => void
    
      delValue: (optId: string | number) => void
    
      showLayer: () => void
    
      hideLayer: () => void
    }, {}, {}, {}, {}, {}, {
      'select': [value: T]
    
      'remove': [value: string | number]
    
      'create': [value: string | number]
    
      'update:modelValue': [value: (string | number)[]]
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
      valueKey?: keyof T
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
    
      'moved': [optionId: string | number, prevOptionId: string | number]
    
      'deleted': [option: T]
    
      'update:modelValue': [value: T[]]
    }>
  export const SortableSelect: DefineComponent<
    {
      modelValue: T[]
      showIcons?: boolean
      options: T[] | OptionsFunc<T>
    }, {}, {}, {}, {}, {}, {}, {
      'update': [_value: T[]]
    
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
      tag?: string
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
    }, {}, {}, {}, {}, {}, {}, {}>
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
      id: number
      directoryId: number,
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
    id: string|number,
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
    id: string|number,
    label?: string;
    options: Array<DropDownOption>;
    className?: string,
  }
  export type OptionsGetterSync = () => DropDownGroupOption[]
  export type OptionsGetterAsync = () => Promise<DropDownGroupOption[]>
  T = any
  export type HSV = { h: number, s: number, v: number, a?: number };
  export enum IconType {
      IssueType = "issuetypes",
      Priority = "priorities",
      Project = "projects",
      Status = "statuses",
  }
  export type OptionsFunc<T> = (term: string | null, selected: Array<ID>) => Promise<Array<T>>;
  export type ID = string|number;
  export interface ActivityStreamSearchQuery extends SearchQuery {
      projectKey?: string
      user?: string
  }
  export interface ActivityStreamItem {
      issue: IssueInfo
      activityStreamTitle: string
      author: User
      comment?: Comment
      worklog?: Worklog
      changeItems?: Array<ChangeItem>
      attachments?: Array<Attachment>
      date: string
  }
  export interface IssueInfo extends IssueKey {
      id: number,
      summary: string,
      issueType: IssueType,
      issueTypeId: number,
      status: Status,
      priority?: Priority,
  }
  export interface IssueType {
      id: number,
      name: string,
      description?: string,
      iconName: string,
      iconUrl: string,
  }
  export interface Status {
      id: number,
      name: string,
      description: string|null,
      category: StatusCategory,
      categoryKey: string,
  }
  export interface StatusCategory {
      key: StatusCategoryEnum,
      name: string,
  }
  export enum StatusCategoryEnum {
      TODO = "todo",
      IN_PROGRESS = "in_progress",
      DONE = "done",
  }
  export interface Priority {
      id: number,
      name: string,
      description?: string,
      iconName?: string,
      iconUrl?: string,
      sequence: number
  }
  export interface Comment {
      id: number,
      author: User,
      created: string,
      updated?: string,
      updater?: User,
      text: string
  }
  export interface Worklog {
      id: number,
      author: User,
      authorId: number,
      created: string
      updater: User|null,
      updaterId: number|null,
      updated: string|null,
      startDate: string,
      timeSpent: string,
      comment: string|null,
  }
  export interface ChangeItem {
      id: number,
      fieldId: string,
      fieldName: string,
      oldStringValue: string,
      newStringValue: string,
  }
  export interface Attachment {
      id: number,
      authorId: number,
      author: User,
      created: string,
      fileName: string,
      fileUrl: string,
      fileSize: number,
      mimeType: string,
      thumbName: string,
      thumbUrl: string,
  }
  export interface ActivityStreamItemGrouped {
      groupedUser: User
      items: Array<ActivityStreamItem>
  }
  export enum ColorStrategy {
      Priority = "priority",
      Query = "queryString",
  }
  export interface Board extends SharedObject {
      id: number,
      name: string;
      authorId: number,
      author: User,
      description: string,
      type: 'kanban'|'scrum',
      columns: Array<BoardColumn>,
      filters: Array<BoardFilter>,
      filterId: number,
      rankField: IssueField,
      rankFieldId: string,
      cardColorStrategy: ColorStrategy,
      cardColorPreset?: CardColorPreset,
      cardExtraFieldIds: Array<string>,
      cardExtraFields: Array<IssueField>,
      detailViewFieldIds: Array<string>,
      detailViewFields: Array<IssueField>,
      doneLimitDays: number,
      activeSprints: Array<Sprint>,
  }
  export interface BoardColumn {
      id: number,
      name: string,
      color: string,
      statusIds: Array<number>,
  }
  export interface BoardFilter {
      id: number,
      name: string,
      description: string,
      queryString: string,
  }
  export interface IssueField {
      id: string,
      name: string,
      description?: string
      fieldTypeKey: string
      fieldTypeName: string,
      editComponent?: string,
      editComponents?: Array<string>,
      viewComponent?: string,
      viewComponents?: Array<string>,
      navigatorComponent?: string,
      navigatorComponents?: Array<string>,
      optionsComponent?: string,
      searcherKey: string
      searcherName: string,
      valid: boolean,
      systemField: boolean,
      contexts?: Array<IssueFieldContext>,
      options?: Array<Option<number>>,
  }
  export interface IssueFieldContext {
      id: number,
      name: string,
      description?: string,
      projectIds?: Array<number>,
      issueTypeIds?: Array<number>,
      projects?: Array<Project>,
      issueTypes?: Array<IssueType>,
      defaultValue?: FieldValue,
      options?: Array<Option<number>>,
  }
  export interface Project {
      id: number,
      key: string,
      name: string,
      description?: string,
      iconName?: string,
      iconUrl?: string,
      canAdminProject: boolean,
      created: string,
      lead: User,
      leadId?: number,
      assignStrategy: AssignStrategy,
  }
  export enum AssignStrategy {
      LEAD = "lead",
      AUTHOR = "author",
  }
  export type FieldValue = string | null | undefined | object | Array<string>
  export interface Option<T> {
      id: T,
      name: string,
      iconUrl?: string,
      altName?: string,
  }
  export interface CardColorPreset {
      strategy: ColorStrategy,
      colors: Array<CardColor>,
  }
  export interface CardColor {
      id: number,
      value: string,
      color: string,
  }
  export interface Sprint {
      id: number;
      name: string;
      author: User,
      authorId: number;
      goal: string;
      boardId: number,
      displayName: string,
      state: SprintState,
      active: boolean,
      complete: boolean,
      startDate: string,
      finishDate: string,
      activatedDate: string,
      completedDate: string,
      auditEntries: Array<SprintAuditEntry>
  }
  export enum SprintState {
      NEW = 'new',
      ACTIVE = 'active',
      COMPLETE = 'complete',
  }
  export interface SprintAuditEntry {
      id: number,
      author: User,
      authorId: number,
      state: SprintState,
      created: string,
  }
  export interface KanbanBoard extends Board {
  
  }
  export interface ScrumBoard extends Board {
  
  }
  export interface BacklogResponse {
      backlogIssues: SearchResult<Issue>,
      sprintIssues: Array<Issue>,
  }
  export interface SearchResult<T> {
      total: number,
      results: Array<T>,
      page: number,
      limit: number,
  }
  export interface Issue extends IssueInfo {
      projectId: number,
      assignee?: User,
      author?: User,
      project: Project,
      priority?: Priority,
      estimate?: string,
      timeLeft?: string,
      timeSpent?: string,
      estimateValue?: number,
      timeLeftValue?: number,
      timeSpentValue?: number,
      priorityId?: number,
      status: Status,
      statusId: number,
      summary: string,
      description: string,
      created: string,
      updated: string,
      color?: string,
      sprint?: Sprint,
      rank?: string,
      watchers: WatchersInfo,
      fields?: {
          [key: string]: FieldValue
      },
      issuePerms: IssuePerms,
      [key: string]: any
  }
  export interface WatchersInfo {
      count: number,
      watching: boolean,
  }
  export interface IssuePerms {
      canEditIssue: boolean,
      canAssignIssue: boolean,
      canTransitIssue: boolean,
      canMoveIssue: boolean,
      canDeleteIssue: boolean,
      canCreateComments: boolean,
      canEditOwnComments: boolean,
      canEditAllComments: boolean,
      canDeleteAllComments: boolean,
      canDeleteOwnComments: boolean,
      canCreateAttachments: boolean,
      canDeleteAllAttachments: boolean,
      canDeleteOwnAttachments: boolean,
      canCreateWorklogs: boolean,
      canEditAllWorklogs: boolean,
      canEditOwnWorklogs: boolean,
      canDeleteAllWorklogs: boolean,
      canDeleteOwnWorklogs: boolean,
      canViewWatchers: boolean,
      canManageWatchers: boolean,
  }
  export interface BoardIssues {
      issues: Array<Issue>,
      total: number,
      limit: number,
  }
  export interface BoardSearchQuery extends SearchQuery {
      type?: 'kanban'|'scrum';
      author?: Array<string>;
      favorite?: 1|0,
  }
  export interface SprintReportResponse {
      sprint: Sprint,
      board: Board,
      issuesMap: Record<string, IssueInfo>,
      chartData: Array<ChartData>,
      statusCategoryMap: Record<string, StatusCategory>,
  }
  export interface ChartData {
      value: string,
      chartValues: Array<ChartValue>,
  }
  export interface ChartValue {
      dateValue: number,
      date: string,
      count: number,
  }
  export interface LoginResponse {
      user?: User,
      error?: string
  }
  export interface Dashboard {
      id: number,
      name: string,
      description: string,
      author: User,
      sharedForGroups: Array<string>,
      sharedForUserIds: Array<number>,
      widgets: Array<DashboardWidget>,
  }
  export interface DashboardWidget extends WidgetPosition {
      id: number,
      widgetType: DashboardWidgetType,
      params: WidgetParams,
  }
  export interface DashboardWidgetType {
      key: string,
      name: string,
      description: string,
      previewImageUrl: string,
      viewComponent: string,
      editComponent: string,
  }
  export type WidgetParams = {[key: string]: string}
  export interface WidgetPosition {
      columns: [number, number],
      rows: [number, number],
  }
  export interface DeleteResult {
      count: number
  }
  export interface ResponseError {
      status: number,
      reason: string,
      errors: Errors
  }
  export interface IssueEventType {
      id: string,
      name: string,
      description: string | null
  }
  export interface Icon {
      name: string,
      iconType: string,
      url: string,
  }
  export interface ExportModule {
      id: string,
      name: string,
      webComponent: string,
  }
  export interface ExportParams {
      moduleId: string,
      limit: number,
      fields: Array<string>,
      qs: string,
      extraParams?: {[key: string]: string},
  }
  export interface FieldTypeDescriptor {
      key: string,
      name: string,
      description?: string,
      iconUrl?: string,
      navigatorComponents?: Array<string>,
      viewComponents?: Array<string>,
      editComponents?: Array<string>,
  }
  export interface FieldSearcher {
      key: string,
      name: string,
      description?: string,
      webComponent: string,
  }
  export interface ChangeGroup {
      id: number,
      author: User,
      created: string,
      changeItems: {[key: string]: ChangeItem},
  }
  export interface IndexingStatus {
      startDate: string,
      finishDate: string,
      time: number,
      total: number,
      completed: number,
      failures: number,
      state: 'calc' | 'indexing' | 'done' | 'fail',
      running: boolean,
      progress: number,
      planningCompleteDate: string,
  }
  export interface IndexingStats {
      indexedIssuesCount: number,
      storedIssuesCount: number,
  }
  export interface IssueTypeSchema {
      id: number,
      name: string,
      description?: string,
      issueTypes: Array<IssueType>,
      issueTypeIds: Array<number>,
  }
  export interface PriorityAffectedSchemas {
      priority: Priority,
      prioritySchemas: Array<PrioritySchema>,
  }
  export interface PrioritySchema {
      id: number,
      name: string,
      description?: string,
      priorities: Array<Priority>,
  }
  export interface Resolution {
      id: number,
      name: string,
      description?: string,
  }
  export interface ResolutionSchema {
      id: number,
      name: string,
      description?: string,
      resolutions: Array<Resolution>,
  }
  export interface IssueKey {
      key: string
      projectKey: string
      issueNum: number
  }
  export interface IssueSearchResult extends SearchResult<Issue> {
      fields: Array<IssueField>
  }
  export interface FieldLayoutItem {
      fieldId: string,
      field: IssueField,
      required: boolean,
  }
  export interface FieldLayout {
      id: number,
      name: string,
      description: string,
      fieldLayoutItems: Array<FieldLayoutItem>
  }
  export interface FieldLayoutSchemaEntry {
      issueType: IssueType,
      fieldLayout: FieldLayout,
  }
  export interface FieldLayoutSchema {
      id: number,
      name: string,
      description: string,
      defaultLayout: FieldLayout,
      defaultLayoutId: number,
      entries: Array<FieldLayoutSchemaEntry>
  }
  export interface IssueLinkType {
      id: number,
      name: string,
      incomingName: string,
      outgoingName: string,
  }
  export interface IssueLink {
      id: number,
      issueLinkTypeId: number,
      issueLinkType: IssueLinkType,
      sourceIssueId: number,
      sourceIssue: Issue,
      targetIssueId: number,
      targetIssue: Issue,
  }
  export interface IssueLinkRequest {
      issueLinkTypeId: string,
      sourceIssueKeys?: Array<String>
      targetIssueKeys?: Array<String>
  }
  export interface MetaIssueField {
      id: string,
      name: string,
      description?: string,
      required: boolean,
      value: FieldValue,
      viewComponent: string,
      editComponent: string,
      editable: boolean,
  }
  export interface MetaIssueTab {
      id: number,
      name: string,
      fields: Array<MetaIssueField>,
  }
  export interface MetaIssueAction {
      name: string,
      actionId: number,
      screenId: number,
      targetStatusId: number,
  }
  export interface MetaIssue {
      tabs: Array<MetaIssueTab>,
      issue?: Issue,
      peopleFields: Array<MetaIssueField>,
      dateFields: Array<MetaIssueField>,
      systemFields: Array<string>,
      actions: Array<MetaIssueAction>,
      actionName: string|null,
      perms: IssuePerms,
      project: Project,
      issueType: IssueType,
      requiredFields: Array<string>,
      descriptionField: MetaIssueField,
      priorityField: MetaIssueField,
  }
  export interface IssueMoveResponse {
      project: Project,
      issueType: IssueType,
      fields: Array<MetaIssueField>,
      issue: Issue,
  }
  export type QueryOperator = '=' | '!=' | '~' | '!~' | 'is' | 'not is' | '>' | '>=' | '<' | '<=';
  export interface QueryParseError {
      line: number,
      position: number,
      message: string
  }
  export interface ParseResult {
      valid: boolean,
      errorMessage: string | null,
      errors: Array<QueryParseError>
  }
  export interface ClauseValue {
      value: string,
      displayValue: string,
  }
  export interface QueryFunction {
      functionName: string,
      list: boolean,
      valueType: string,
      clauseValueType: string,
      minimumRequiredArguments: boolean,
  }
  export interface QuerySearcher {
      fieldId: string,
      fieldName: string,
      valueType: string,
      allowedOperators: Array<QueryOperator>,
  }
  export interface QuerySorter {
      fieldId: string,
      fieldName: string,
  }
  export interface TimeSheerReportRequest {
      projectOrFilter: string|number,
      startDate?: string,
      finishDate?: string,
  }
  export interface TimeSheetIssueRow {
      issue: IssueInfo,
      values: Record<string, string>
      total: string,
  }
  export interface TimeSheetCol {
      id: string,
      name: string,
      title: string,
      iconUrl: string,
  }
  export interface TimeSheetIssueReportResponse {
      rows: Array<TimeSheetIssueRow>;
      columns: Array<TimeSheetCol>;
      totalTimeSpent: string;
      totals: Record<string, string>;
  }
  export interface StatisticsGroup {
      id: string,
      label: string,
      iconUrl?: string,
      count: number,
      qs: string
  }
  export interface StatisticsGroupResult {
      keys: Array<string>,
      groups: Array<StatisticsGroup>,
      issueField: IssueField,
      totalCount: number,
  }
  export interface AffectedSchemas {
    issueTypeSchemas: Array<IssueTypeSchema>
    workflowSchemas: Array<WorkflowSchema>
    issueTypeScreenSchemas: Array<IssueTypeScreenSchema>
    fieldLayoutSchemas: Array<FieldLayoutSchema>
  }
  export interface WorkflowSchema {
      id: number,
      name: string,
      description: string|null,
      defaultWorkflow: Workflow,
      defaultWorkflowId: number,
      entries: Array<WorkflowSchemaEntry>,
  }
  export interface Workflow {
      id: number,
      name: string,
      description: string | null,
      statuses: Array<Status>,
      display: {
          statusPositionMap: {
              [kay: number]: Position
          },
          canvasPosition: Position,
          actionPorts: {
              [key: number]: {
                  left: NodePort,
                  right: NodePort,
              }
          },
      },
      createAction: WorkflowAction,
      actions: Array<WorkflowAction>,
      originalId: number | null,
      author: User | null,
      updater: User | null,
      created: string | null,
      updated: string | null,
  }
  export interface Position {
      x: number,
      y: number,
  }
  export interface NodePort {
      nodeId: number,
      direction: Direction|null,
      index: number,
  }
  export type Direction = 'left' | 'right' | 'top' | 'bottom';
  export interface WorkflowAction {
      id: number,
      name: string,
      sourceStatusIds: Array<number>,
      targetStatusId: number,
      screenId: number|null,
      screen: Screen|null,
      validators: Array<WorkflowActionFunction>,
      conditions: WorkflowActionConditionGroup,
      postfunctions: Array<WorkflowActionFunction>,
  }
  export interface Screen {
      id: number,
      name: string,
      description: string,
      tabs: Array<ScreenTab>
  }
  export interface ScreenTab {
      id: number,
      name: string,
      fieldIds: Array<string>,
      fields: Array<IssueField>,
  }
  export interface WorkflowActionFunction extends WorkflowFunctionDescriptor {
      id: string,
      params: null | WorkflowFunctionParams,
      viewParams: null | WorkflowFunctionViewParams,
  }
  export interface WorkflowFunctionParams {
      [key: string]: string
  }
  export interface WorkflowFunctionViewParams {
      [key: string]: any
  }
  export interface WorkflowActionConditionGroup {
      id: string,
      condition: WorkflowActionFunction | null,
      items: Array<WorkflowActionConditionGroup> | null,
      operator: ConditionOperator,
  }
  export type ConditionOperator = "and" | "or";
  export interface WorkflowSchemaEntry {
      issueType: IssueType,
      workflow: Workflow,
  }
  export interface IssueTypeScreenSchema {
      id: number,
      name: string,
      description?: string | null,
      defaultScreenSchema: ScreenSchema,
      defaultScreenSchemaId: number,
      entries: {
          [key: number]: ScreenSchema
      },
      assignedIssueTypesMap: {[key: number]: IssueType}
  }
  export interface ScreenSchema {
      id: number,
      name: string,
      description?: string | null,
      defaultScreen: Screen,
      defaultScreenId: number,
      createScreen?: Screen,
      createScreenId?: number,
      editScreen?: Screen,
      editScreenId?: number,
      viewScreen?: Screen,
      viewScreenId?: number,
  }
  export interface ScreenSchemaAffectedSchemas {
      screenSchema: ScreenSchema,
      issueTypeScreenSchemas: Array<IssueTypeScreenSchema>,
  }
  export interface SearchFilter extends SharedObject {
      id: number,
      name: string,
      description: string,
      queryString: string,
      author: User,
      authorId: number,
      updateAuthorId?: number,
      created: string,
      updated?: string,
  
  }
  export interface SearchFilterQueryParams extends SearchQuery {
      author?: Array<number>,
      favorite?: 0|1,
  }
  export interface WorkflowFunction {
      name: string,
      description: string,
      className: string,
      allowMultiple: boolean,
  }
  export interface WorkflowFunctionDescriptor extends WorkflowFunction {
      viewComponent: string | null,
      editComponent: string | null,
  }
  export type WorkflowFunctionType = 'postfunction' | 'validator' | 'condition';
  export interface WorkflowSchemaSimple {
      id: number,
      name: string,
      description: string|null,
      workflowIds: Array<number>,
  }
  export interface WorkflowSearchResult extends SearchResult<Workflow>{
      drafts: Array<Workflow>,
      schemas: Array<WorkflowSchemaSimple>,
  }
  export interface Directory {
      id: number
      name: string,
      description: string,
      directoryTypeKey: string,
      directoryType: DirectoryType,
      attributes: DirectoryAttributes,
      active: boolean,
      sequence: number,
  }
  export interface DirectoryType {
      key: string,
      name: string,
      description: string,
      webComponent: string,
  }
  export type DirectoryAttributes = {[key: string]: string};
  export interface Group {
      id: number,
      name: string,
  }
  export interface IncludedBy {
      user: boolean,
      groups: Array<string>,
  }
  export interface ProjectRoleMap {
      projectId: number;
      projectName: string;
      projectKey: string;
      rolesMap: Record<number, IncludedBy>
  }
  export interface UserProjectRoles {
      user: User,
      projectRoles: Array<ProjectRole>,
      projectRoleMaps: Array<ProjectRoleMap>,
  }
  export interface ProjectRole {
      id: number,
      name: string,
      description?: string
  }
  export interface UserActivity {
      dashboardId: number|null,
      navigatorQs: string | null,
      navigatorFilterId: number | null,
      navigatorColumnIds: Array<string>,
      lastVisitedIssuesIds: Array<number>,
      lastVisitedProjectIds: Array<number>,
      navigatorColumns: Array<IssueField>,
      lastVisitedFilterIds: Array<number>,
      lastVisitedFilters: Array<SearchFilter>,
      lastVisitedBoardIds: Array<number>,
      favoriteFilterIds: Array<number>,
      favoriteBoardIds: Array<number>,
  }
  export interface UserActivityRequest {
      navigatorColumnIds: Array<string>,
      favoriteFilterIds: Array<number>,
      unFavoriteFilterIds: Array<number>,
      favoriteBoardIds: Array<number>,
      unFavoriteBoardIds: Array<number>,
  }
  export interface UserProfile extends User {
      activity: UserActivity,
      admin: boolean,
      viewUsers: boolean,
  }
  export interface PortPosition extends Position {
      direction?: Direction|null,
      index: number,
  }
  export interface Curve {
      id: string,
      sx: number,
      sy: number,
      sd?: Direction|null,
      ex: number,
      ey: number,
      ed?: Direction|null,
  }
  export interface NodeModel {
      id: number,
      position: Position,
      name: string,
      startNode: boolean,
      leftPorts: number,
      topPorts: number,
      rightPorts: number,
      bottomPorts: number,
  }
  export interface NodeLink {
      id: string,
      name: string,
      source: NodePort,
      target: NodePort,
  }
  export interface WorkflowLink extends NodeLink {
      actionId: number,
  }
  export interface BaseOption {
      name: string;
      [key: string]: any;
  }
  export interface ProjectWithIssueTypes extends Project {
      issueTypes: Array<IssueType>
  }
  export interface ProjectWithSchemaIds extends Project {
      issueTypeSchemaId: number | null,
      resolutionSchemaId: number | null,
      issueTypeScreenSchemaId: number | null,
      fieldLayoutSchemaId: number | null,
      prioritySchemaId: number | null,
      workflowSchemaId: number|null,
      permissionSchemaId: number|null,
      notificationSchemaId: number|null,
  }
  export interface ProjectWithSchemas extends Project {
      issueTypeSchemaId: number | null,
      resolutionSchemaId: number | null,
      issueTypeScreenSchemaId: number | null,
      fieldLayoutSchemaId: number | null,
      fieldLayoutSchema: FieldLayoutSchema | null,
      prioritySchemaId: number | null,
      issueTypeSchema: IssueTypeSchema | null,
      resolutionSchema: ResolutionSchema | null,
      issueTypeScreenSchema: IssueTypeScreenSchema | null,
      prioritySchema: PrioritySchema | null,
      workflowSchemaId: number|null,
      workflowSchema: WorkflowSchema|null,
      permissionSchemaId: number|null,
      permissionSchema: PermissionSchema|null,
      notificationSchemaId: number|null,
      notificationSchema: NotificationSchema|null,
  }
  export interface PermissionSchema {
      id: number,
      name: string,
      description: string|null,
      permissionsMap: {
          [key: string]: PermissionSchemaEntry
      }
  }
  export interface PermissionSchemaEntry {
      userIds: Array<number>,
      groupNames: Array<string>,
      projectRoleIds: Array<number>,
      userFieldIds: Array<string>,
  }
  export interface NotificationSchema {
      id: number,
      name: string,
      description: string|null,
      notificationsMap: {
          [key: string]: NotificationSchemaEntry
      }
  }
  export interface NotificationSchemaEntry {
      userIds: Array<number>,
      groupNames: Array<string>,
      projectRoleIds: Array<string>,
      userFieldIds: Array<string>,
  }
  export interface ProjectMembers {
      project: Project,
      projectRoles: Array<ProjectRole>,
      usersMap: {
          [key: number]: Array<User>
      },
      groupsMap: {
          [key: number]: Array<string>
      },
  }
  export interface ProjectTemplate {
      name: string,
      description: string,
      iconUrl: string,
  }
  export interface ReportCategory {
      id: string,
      name: string,
  }
  export interface Report {
      id: string;
      name: string;
      category: ReportCategory;
      description: string;
      thumbnailUrl: string;
      webComponent: string;
      weight: number;
  }
  export interface SearchResultWithSchemas<T, S> extends SearchResult<T> {
    schemas: Array<S>
  }
  export interface ScreenSearchResult extends SearchResultWithSchemas<Screen, ScreenSchema> {
      workflows: Array<Workflow>
  }
  export interface SearchQuery extends LocationQueryRaw {
      term?: string,
      page?: number,
      limit?: number,
  }
  export interface ScreenSearchQuery extends SearchQuery {
      fieldIds?: Array<string>
  }
  export interface UserSearchQuery extends SearchQuery {
      directoryIds?: Array<string>,
      groups?: Array<string>
  }
  export interface IssueSearchQuery extends SearchQuery {
      filter?: number,
      qs?: string,
      fields?: Array<string>
  }
  export interface SearchQueryExt {
      term?: string,
      page?: number,
      limit?: number,
      excludes?: Array<string|number>
  }
  export interface ProjectOrFilter {
      filterId?: string,
      projectKey?: string
  }
  export interface ShareEntry  {
      users: Array<User>;
      userIds: Array<number>;
      groupNames: Array<string>;
  }
  export interface ApplicationProperties {
      baseUrl: string,
      applicationTitle: string,
      defaultLocale: string,
      indexingLocale: string,
  }
  export interface SystemInfo {
    groups: Array<SystemInfoGroup>;
  }
  export interface SystemInfoGroup {
    key: string,
    label: string,
    entries: Array<SystemInfoEntry>,
  }
  export interface SystemInfoEntry {
    key: string,
    label: string,
    value: string,
  }
  export interface LoggingEntry {
    id: string,
    packageName: string,
    level: string,
  }
  export interface IncomingMailServer {
      id: number,
      name: string,
      description: string,
      protocol: 'pop3' | 'imap',
      useSSL: boolean,
      startTLS: boolean,
      timeout: number,
      host: string,
      port: number,
      login: string,
      password: string,
  }
  export interface OutgoingMailServer {
      id: number,
      name: string,
      description: string | null,
      fromEmail: string,
      subjectPrefix: string | null,
      host: string,
      port: number,
      timeout: number,
      startTLS: boolean,
      useSSL: boolean,
      username: string,
      password?: string,
  }
  export interface OutgoingMailServerRequest extends OutgoingMailServer {
      subject: string,
      to: string,
      body: string,
  }
  export interface Mail {
      subject: string,
      from: string,
      to: Array<string>,
      body: string,
      contentType: string,
  }
  export interface QueueMail extends Mail {
      id: number,
      mailServerId: number,
      created: string,
      state: string,
      errorMessage: string,
  }
  export interface MessageHandlerFactory {
      key: string,
      name: string,
      description: string,
      webComponent: string,
  }
  export interface MessageHandlerModel {
      id: number,
      name: string,
      description: string,
      factory: MessageHandlerFactory,
      factoryKey: string,
      mailServer: IncomingMailServer,
      mailServerId: number,
      handlerParams: Record<string, string>,
      delayMinutes: number,
      folderName: string,
      enabled: boolean,
  }
  export interface NotificationSchemaResolved extends NotificationSchema {
      notificationsMap: {
          [key: string]: NotificationSchemaEntryResolved
      }
  }
  export interface NotificationSchemaEntryResolved extends NotificationSchemaEntry {
      users: Array<User>,
      projectRoles: Array<ProjectRole>,
      userFields: Array<IssueField>,
  }
  export interface Permission {
      id: string,
      name: string,
      description: string|null,
  }
  export interface PermissionSchemaResolved extends PermissionSchema {
      permissionsMap: {
          [key: string]: PermissionSchemaEntryResolved
      }
  }
  export interface PermissionSchemaEntryResolved extends PermissionSchemaEntry {
      users: Array<User>,
      projectRoles: Array<ProjectRole>,
      userFields: Array<IssueField>,
  }
  export interface GlobalPermissions {
      adminGroups: Array<string>,
      allowedLoginGroups: Array<string>,
      allowedViewUsersGroups: Array<string>,
  }
  export interface SharedObject {
      hasEditAccess: boolean,
      hasViewAccess: boolean,
      viewPerms: ShareEntry,
      editPerms: ShareEntry,
  }
  export interface PluginModel {
      key: string,
      name: string,
      description: string,
      version: string,
      vendor: PluginVendor,
      enabled: boolean,
      components: Array<PluginComponent>
      resources: Array<PluginResource>
  }
  export interface PluginVendor {
      name?: string,
      url?: string,
  }
  export interface PluginComponent {
      key: string,
      name: string,
      className: string,
      resources: Array<PluginResource>,
      enabled: boolean,
  }
  export interface PluginResource {
      pluginKey: string,
      key: string,
      type: 'comp'|'css'|'i18n'|'any',
      path: string,
      enabled: boolean,
      resourceUrl: string,
  }
  export interface RankingStatus {
    startDate: string,
    finishDate: string,
    time: number,
    total: number,
    completed: number,
    state: 'calc' | 'ranking' | 'done',
    running: boolean,
    planningCompleteDate: string,
    progress: number,
    rankField: IssueField,
  }
  export interface RankingStats {
    issueField: IssueField,
    missingCount: number,
  }
  export interface Scheduler {
    cronExpression?: string,
    runOnce: boolean,
    interval?: string,
  }
  export interface JobDetails {
    jobId: string,
    jobRunnerKey: string,
    schedule: Scheduler,
    prevRun: string,
    nextRun: string,
    running: boolean,
  }
  export interface JobRunnerResponse {
    name: string,
    description: string,
    jobRunnerKey: string,
    jobs: Array<JobDetails>,
  }
  export interface JobResult {
    jobId: string,
    jobRunnerKey: string,
    startDate: string,
    executionTime: number,
    result: 'successful' | 'failed' | 'aborted',
    message: string,
    stackTrace: string,
  }
  export interface SystemState {
      ok: boolean,
      state: string,
      progress?: number,
      plannedCompleteDate?: string,
  }
  export interface WebNode {
      id: number,
      name: string,
      routeName: string,
      routeParams: {[key: string]: string},
      routeQuery: {[key: string]: string},
      iconUrl: string,
      iconTitle: string,
      hint: string,
      children: Array<WebNode>,
      section: boolean,
  }
  export interface WebRoute {
      routeName: string,
      routePath: string,
      componentUrl: string,
      children: Array<WebRoute>,
      parent: string,
  }
}

