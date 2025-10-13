<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-crosshairs-gps</v-icon>
            Coordinates Management
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              @click="openCreateDialog"
              prepend-icon="mdi-plus"
            >
              Add Coordinates
            </v-btn>
          </v-card-title>

          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="searchQuery"
                  label="Search coordinates..."
                  prepend-inner-icon="mdi-magnify"
                  clearable
                  @input="onSearch"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-card-text>

          <v-data-table
            :headers="headers"
            :items="coordinates"
            :loading="loading"
            :items-per-page="tableState.size"
            :page="tableState.page + 1"
            :server-items-length="totalElements"
            @update:page="onPageChange"
            @update:items-per-page="onItemsPerPageChange"
            @update:sort-by="onSortChange"
            class="elevation-1"
          >
            <template v-slot:item.actions="{ item }">
              <v-btn
                icon="mdi-pencil"
                size="small"
                @click="editCoordinates(item)"
                class="mr-1"
              ></v-btn>
              <v-btn
                icon="mdi-delete"
                size="small"
                color="error"
                @click="deleteCoordinates(item)"
              ></v-btn>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>

    <CoordinatesEditDialog
      v-model="dialog.show"
      :mode="dialog.mode"
      :coordinates="dialog.data"
      @save="onCoordinatesSave"
      @close="closeDialog"
    />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useTableStore } from '@/stores/table'
import { useNotificationStore } from '@/stores/notification'
import { useRealtime } from '@/services/realtime'
import { coordinatesApi } from '@/services/api'
import type { CoordinatesResponse, SearchRequest } from '@/types'
import CoordinatesEditDialog from '@/components/CoordinatesEditDialog.vue'

const tableStore = useTableStore()
const notificationStore = useNotificationStore()
const { subscribe, unsubscribe } = useRealtime()

const coordinates = ref<CoordinatesResponse[]>([])
const loading = ref(false)
const totalElements = ref(0)
const searchQuery = ref('')

const tableState = computed(() => tableStore.getTableState('coordinates'))

const dialog = ref({
  show: false,
  mode: 'create' as 'create' | 'edit',
  data: null as CoordinatesResponse | null
})

const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'X', key: 'x', sortable: true },
  { title: 'Y', key: 'y', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false }
]

const loadCoordinates = async () => {
  loading.value = true
  try {
    const searchRequest: SearchRequest = {
      page: tableState.value.page,
      size: tableState.value.size,
      sort: tableState.value.sort,
      filters: buildFilters()
    }

    const response = await coordinatesApi.searchCoordinates(searchRequest)
    coordinates.value = response.data.content || []
    totalElements.value = response.data.totalElements || 0
  } catch (error) {
    console.error('Error loading coordinates:', error)
    notificationStore.showNotification('Failed to load coordinates', 'error')
  } finally {
    loading.value = false
  }
}

const buildFilters = () => {
  const filters = []
  
  if (searchQuery.value) {
    filters.push({ field: 'x', pattern: searchQuery.value })
  }
  
  return filters
}

const onSearch = () => {
  tableStore.setSearch('coordinates', searchQuery.value)
  loadCoordinates()
}

const onPageChange = (page: number) => {
  tableStore.setPage('coordinates', page - 1)
  loadCoordinates()
}

const onItemsPerPageChange = (size: number) => {
  tableStore.setSize('coordinates', size)
  loadCoordinates()
}

const onSortChange = (sortBy: any[]) => {
  const sort = sortBy.map(item => ({
    field: item.key,
    direction: item.order === 'asc' ? 'ASC' : 'DESC'
  }))
  tableStore.setSort('coordinates', sort)
  loadCoordinates()
}

const openCreateDialog = () => {
  dialog.value = {
    show: true,
    mode: 'create',
    data: null
  }
}

const editCoordinates = (coordinates: CoordinatesResponse) => {
  dialog.value = {
    show: true,
    mode: 'edit',
    data: coordinates
  }
}

const deleteCoordinates = async (coordinates: CoordinatesResponse) => {
  if (confirm(`Are you sure you want to delete coordinates (${coordinates.x}, ${coordinates.y})?`)) {
    try {
      await coordinatesApi.deleteCoordinates(coordinates.id!)
      notificationStore.showNotification('Coordinates deleted successfully', 'success')
      loadCoordinates()
    } catch (error) {
      console.error('Error deleting coordinates:', error)
      notificationStore.showNotification('Failed to delete coordinates', 'error')
    }
  }
}

const onCoordinatesSave = () => {
  closeDialog()
  loadCoordinates()
}

const closeDialog = () => {
  dialog.value.show = false
}

const handleRealtimeUpdate = (event: any) => {
  if (event.entity === 'COORDINATES') {
    loadCoordinates()
  }
}

onMounted(() => {
  loadCoordinates()
  subscribe('COORDINATES', handleRealtimeUpdate)
})

watch(() => tableState.value, () => {
  loadCoordinates()
}, { deep: true })
</script>
