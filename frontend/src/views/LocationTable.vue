<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-map-marker</v-icon>
            Locations Management
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              @click="openCreateDialog"
              prepend-icon="mdi-plus"
            >
              Add Location
            </v-btn>
          </v-card-title>

          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="searchQuery"
                  label="Search locations..."
                  prepend-inner-icon="mdi-magnify"
                  clearable
                  @input="onSearch"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-card-text>

          <v-data-table
            :headers="headers"
            :items="locations"
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
                @click="editLocation(item)"
                class="mr-1"
              ></v-btn>
              <v-btn
                icon="mdi-delete"
                size="small"
                color="error"
                @click="deleteLocation(item)"
              ></v-btn>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>

    <LocationEditDialog
      v-model="dialog.show"
      :mode="dialog.mode"
      :location="dialog.data"
      @save="onLocationSave"
      @close="closeDialog"
    />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useTableStore } from '@/stores/table'
import { useNotificationStore } from '@/stores/notification'
import { useRealtime } from '@/services/realtime'
import { locationsApi } from '@/services/api'
import type { LocationResponse, SearchRequest } from '@/types'
import LocationEditDialog from '@/components/LocationEditDialog.vue'

const tableStore = useTableStore()
const notificationStore = useNotificationStore()
const { subscribe, unsubscribe } = useRealtime()

const locations = ref<LocationResponse[]>([])
const loading = ref(false)
const totalElements = ref(0)
const searchQuery = ref('')

const tableState = computed(() => tableStore.getTableState('locations'))

const dialog = ref({
  show: false,
  mode: 'create' as 'create' | 'edit',
  data: null as LocationResponse | null
})

const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'X', key: 'x', sortable: true },
  { title: 'Y', key: 'y', sortable: true },
  { title: 'Z', key: 'z', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false }
]

const loadLocations = async () => {
  loading.value = true
  try {
    const searchRequest: SearchRequest = {
      page: tableState.value.page,
      size: tableState.value.size,
      sort: tableState.value.sort,
      filters: buildFilters()
    }

    const response = await locationsApi.searchLocations(searchRequest)
    locations.value = response.data.content || []
    totalElements.value = response.data.totalElements || 0
  } catch (error) {
    console.error('Error loading locations:', error)
    notificationStore.showNotification('Failed to load locations', 'error')
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
  tableStore.setSearch('locations', searchQuery.value)
  loadLocations()
}

const onPageChange = (page: number) => {
  tableStore.setPage('locations', page - 1)
  loadLocations()
}

const onItemsPerPageChange = (size: number) => {
  tableStore.setSize('locations', size)
  loadLocations()
}

const onSortChange = (sortBy: any[]) => {
  const sort = sortBy.map(item => ({
    field: item.key,
    direction: item.order === 'asc' ? 'ASC' : 'DESC'
  }))
  tableStore.setSort('locations', sort)
  loadLocations()
}

const openCreateDialog = () => {
  dialog.value = {
    show: true,
    mode: 'create',
    data: null
  }
}

const editLocation = (location: LocationResponse) => {
  dialog.value = {
    show: true,
    mode: 'edit',
    data: location
  }
}

const deleteLocation = async (location: LocationResponse) => {
  if (confirm(`Are you sure you want to delete location (${location.x}, ${location.y}, ${location.z})?`)) {
    try {
      await locationsApi.deleteLocation(location.id!)
      notificationStore.showNotification('Location deleted successfully', 'success')
      loadLocations()
    } catch (error) {
      console.error('Error deleting location:', error)
      notificationStore.showNotification('Failed to delete location', 'error')
    }
  }
}

const onLocationSave = () => {
  closeDialog()
  loadLocations()
}

const closeDialog = () => {
  dialog.value.show = false
}

const handleRealtimeUpdate = (event: any) => {
  if (event.entity === 'LOCATION') {
    loadLocations()
  }
}

onMounted(() => {
  loadLocations()
  subscribe('LOCATION', handleRealtimeUpdate)
})

watch(() => tableState.value, () => {
  loadLocations()
}, { deep: true })
</script>
