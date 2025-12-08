import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TableState } from '@/types'

export const useTableStore = defineStore('table', () => {
  const tableStates = ref<Map<string, TableState>>(new Map())

  const getTableState = (tableName: string): TableState => {
    if (!tableStates.value.has(tableName)) {
      tableStates.value.set(tableName, {
        page: 0,
        size: 10,
        sort: [],
        filters: [],
        search: ''
      })
    }
    return tableStates.value.get(tableName)!
  }

  const updateTableState = (tableName: string, updates: Partial<TableState>) => {
    const currentState = getTableState(tableName)
    const newState = { ...currentState, ...updates }
    tableStates.value.set(tableName, newState)
  }

  const resetTableState = (tableName: string) => {
    tableStates.value.set(tableName, {
      page: 0,
      size: 10,
      sort: [],
      filters: [],
      search: ''
    })
  }

  const setPage = (tableName: string, page: number) => {
    updateTableState(tableName, { page })
  }

  const setSize = (tableName: string, size: number) => {
    updateTableState(tableName, { size, page: 0 })
  }

  const setSort = (tableName: string, sort: TableState['sort']) => {
    updateTableState(tableName, { sort })
  }

  const setFilters = (tableName: string, filters: TableState['filters']) => {
    updateTableState(tableName, { filters, page: 0 })
  }

  const setSearch = (tableName: string, search: string) => {
    updateTableState(tableName, { search, page: 0 })
  }

  return {
    getTableState,
    updateTableState,
    resetTableState,
    setPage,
    setSize,
    setSort,
    setFilters,
    setSearch
  }
})
