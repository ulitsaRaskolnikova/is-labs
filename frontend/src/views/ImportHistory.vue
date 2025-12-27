<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-history</v-icon>
            Import History
          </v-card-title>

          <v-card-text>
            <v-data-table
              :headers="headers"
              :items="historyItems"
              :loading="loading"
              :items-per-page="tableState.size"
              :page="tableState.page + 1"
              :server-items-length="totalElements"
              :sort-by="defaultSortBy"
              @update:options="onOptionsChange"
              class="elevation-1"
            >
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  size="small"
                >
                  {{ item.status }}
                </v-chip>
              </template>
              <template v-slot:item.processedCount="{ item }">
                <span v-if="item.status === 'SUCCESS'">
                  {{ item.processedCount }}
                </span>
                <span v-else class="text-grey">-</span>
              </template>
              <template v-slot:item.errorCount="{ item }">
                <v-chip
                  :color="item.errorCount > 0 ? 'error' : 'success'"
                  size="small"
                >
                  {{ item.errorCount }}
                </v-chip>
              </template>
              <template v-slot:item.importDate="{ item }">
                {{ formatDate(item.importDate) }}
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  v-if="item.storagePath"
                  icon
                  size="small"
                  color="primary"
                  @click="downloadFile(item.id, item.fileName)"
                  :disabled="loading"
                >
                  <v-icon>mdi-download</v-icon>
                </v-btn>
                <v-tooltip v-else text="File not available">
                  <template v-slot:activator="{ props }">
                    <v-icon v-bind="props" color="grey">mdi-download-off</v-icon>
                  </template>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useTableStore } from '@/stores/table'
import { useNotificationStore } from '@/stores/notification'
import apiClient from '@/services/api'
import type { SearchRequest } from '@/types'

interface FileImportHistoryResponse {
  id: number
  fileName: string
  status: string
  processedCount: number | null
  errorCount: number
  storagePath: string | null
  importDate: string | number[] | null
}

const notificationStore = useNotificationStore()
const tableStore = useTableStore()

const historyItems = ref<FileImportHistoryResponse[]>([])
const loading = ref(false)
const totalElements = ref(0)

const tableState = computed(() => tableStore.getTableState('importHistory'))

const defaultSortBy = [{ key: 'importDate', order: 'desc' }]

const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'File Name', key: 'fileName', sortable: true },
  { title: 'Status', key: 'status', sortable: true },
  { title: 'Processed Count', key: 'processedCount', sortable: false },
  { title: 'Error Count', key: 'errorCount', sortable: true },
  { title: 'Import Date', key: 'importDate', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false, width: '100px' }
]

const getStatusColor = (status: string) => {
  switch (status) {
    case 'SUCCESS':
      return 'success'
    case 'FAILED':
      return 'error'
    default:
      return 'grey'
  }
}

const formatDate = (dateValue: string | number[] | null | undefined) => {
  if (!dateValue) return '-'
  
  let dateString: string
  
  if (Array.isArray(dateValue)) {
    const [year, month, day, hour, minute, second] = dateValue
    dateString = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second || 0).padStart(2, '0')}`
  } else {
    dateString = String(dateValue)
  }
  
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return dateString
    }
    return date.toLocaleString()
  } catch (e) {
    return dateString
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    const searchRequest: SearchRequest = {
      page: tableState.value.page,
      size: tableState.value.size,
      sort: tableState.value.sort
    }

    const response = await apiClient.post('/import-history/search', searchRequest)
    historyItems.value = response.data.content || []
    totalElements.value = response.data.totalElements || 0
  } catch (error: any) {
    console.error('Error loading import history:', error)
    notificationStore.showNotification(
      'Failed to load import history',
      'error'
    )
  } finally {
    loading.value = false
  }
}

const onOptionsChange = (options: any) => {
  if (options.sortBy && options.sortBy.length > 0) {
    tableStore.setSort('importHistory', [{
      field: options.sortBy[0].key,
      direction: options.sortBy[0].order === 'desc' ? 'DESC' : 'ASC'
    }])
  } else {
    tableStore.setSort('importHistory', [])
  }
  if (options.page !== undefined) {
    tableStore.setPage('importHistory', options.page - 1)
  }
  if (options.itemsPerPage !== undefined) {
    tableStore.setSize('importHistory', options.itemsPerPage)
  }
  loadHistory()
}

const downloadFile = async (id: number, fileName: string) => {
  try {
    const response = await apiClient.get(`/import-history/${id}/file`, {
      responseType: 'blob'
    })

    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    notificationStore.showNotification(
      'File download started',
      'success'
    )
  } catch (error: any) {
    console.error('Error downloading file:', error)
    let errorMessage = 'Failed to download file'
    if (error.response?.data) {
      try {
        const text = await error.response.data.text()
        const errorData = JSON.parse(text)
        errorMessage = errorData.error || errorMessage
      } catch {
        errorMessage = error.response.status === 404 
          ? 'File not found' 
          : 'Failed to download file'
      }
    }
    notificationStore.showNotification(errorMessage, 'error')
  }
}

onMounted(() => {
  const currentState = tableStore.getTableState('importHistory')
  if (!currentState.sort || currentState.sort.length === 0) {
    tableStore.setSort('importHistory', [{
      field: 'importDate',
      direction: 'DESC'
    }])
  }
  loadHistory()
})
</script>

