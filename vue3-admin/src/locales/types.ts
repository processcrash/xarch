import 'vue-i18n'

declare module 'vue-i18n' {
  interface DefineLocaleMessage {
    common: {
      confirm: string
      cancel: string
      submit: string
      reset: string
      search: string
      add: string
      edit: string
      delete: string
      view: string
      save: string
      refresh: string
      export: string
      import: string
      download: string
      upload: string
      selectAll: string
      batchDelete: string
      batchActions: string
      enableSelected: string
      disableSelected: string
      deleteSelected: string
      advanced: string
      yes: string
      no: string
      pleaseSelect: string
      pleaseEnter: string
      all: string
      to: string
      success: string
      failed: string
      operationFailed: string
      confirmDelete: string
      status: {
        active: string
        disabled: string
        read: string
        unread: string
        enabled: string
        pending: string
        approved: string
        rejected: string
        executed: string
      }
      messages: {
        createdSuccess: string
        updatedSuccess: string
        deletedSuccess: string
        operationSuccess: string
        loadFailed: string
        pleaseSelectItems: string
        pleaseSelectToDelete: string
      }
      pagination: {
        total: string
        page: string
        items: string
        prev: string
        next: string
      }
    }
    nav: Record<string, string>
    auth: Record<string, string>
    user: Record<string, any>
    role: Record<string, any>
    menu: Record<string, string>
    dept: Record<string, string>
    dict: Record<string, string>
    config: Record<string, string>
    log: Record<string, string>
    file: Record<string, string>
    message: Record<string, any>
    monitor: Record<string, any>
    resource: Record<string, any>
    tempFile: Record<string, any>
    client: Record<string, any>
    audit: Record<string, any>
    excel: Record<string, any>
    home: Record<string, any>
    validation: Record<string, string>
    lang: {
      zhCN: string
      enUS: string
      switch: string
    }
  }

  interface DefineDateTimeFormat {}

  interface DefineNumberFormat {}
}

export {}
