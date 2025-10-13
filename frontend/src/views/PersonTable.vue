<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-account</v-icon>
            Persons Management
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              @click="openCreateDialog"
              prepend-icon="mdi-plus"
            >
              Add Person
            </v-btn>
          </v-card-title>

          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="searchQuery"
                  label="Search persons..."
                  prepend-inner-icon="mdi-magnify"
                  clearable
                  @input="onSearch"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedEyeColor"
                  :items="eyeColorOptions"
                  label="Filter by Eye Color"
                  clearable
                  @update:model-value="onFilterChange"
                ></v-select>
              </v-col>
              <v-col cols="12" md="3">
                <v-select
                  v-model="selectedHairColor"
                  :items="hairColorOptions"
                  label="Filter by Hair Color"
                  clearable
                  @update:model-value="onFilterChange"
                ></v-select>
              </v-col>
            </v-row>
          </v-card-text>

          <v-data-table
            :headers="headers"
            :items="persons"
            :loading="loading"
            :items-per-page="tableState.size"
            :page="tableState.page + 1"
            :server-items-length="totalElements"
            @update:page="onPageChange"
            @update:items-per-page="onItemsPerPageChange"
            @update:sort-by="onSortChange"
            class="elevation-1"
          >
            <template v-slot:item.eyeColor="{ item }">
              <v-chip :color="getColorChipColor(item.eyeColor)" small>
                {{ item.eyeColor }}
              </v-chip>
            </template>

            <template v-slot:item.hairColor="{ item }">
              <v-chip :color="getColorChipColor(item.hairColor)" small>
                {{ item.hairColor }}
              </v-chip>
            </template>

            <template v-slot:item.nationality="{ item }">
              <v-chip v-if="item.nationality" color="blue" small>
                {{ item.nationality }}
              </v-chip>
              <span v-else>-</span>
            </template>

            <template v-slot:item.height="{ item }">
              {{ item.height ? `${item.height} cm` : '-' }}
            </template>

            <template v-slot:item.creationDate="{ item }">
              {{ formatDate(item.creationDate) }}
            </template>

            <template v-slot:item.actions="{ item }">
              <v-btn
                icon="mdi-eye"
                size="small"
                @click="viewPerson(item)"
                class="mr-1"
              ></v-btn>
              <v-btn
                icon="mdi-pencil"
                size="small"
                @click="editPerson(item)"
                class="mr-1"
              ></v-btn>
              <v-btn
                icon="mdi-delete"
                size="small"
                color="error"
                @click="deletePerson(item)"
              ></v-btn>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>

    <PersonDialog
      v-model="dialog.show"
      :mode="dialog.mode"
      :person="dialog.data"
      @save="onPersonSave"
      @close="closeDialog"
    />

    <PersonDetailsDialog
      v-model="detailsDialog.show"
      :person="detailsDialog.data"
      @close="closeDetailsDialog"
    />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useTableStore } from '@/stores/table'
import { useNotificationStore } from '@/stores/notification'
import { useRealtime } from '@/services/realtime'
import { personsApi } from '@/services/api'
import type { PersonResponse, Color, Country, SearchRequest } from '@/types'
import PersonDialog from '@/components/PersonDialog.vue'
import PersonDetailsDialog from '@/components/PersonDetailsDialog.vue'

const tableStore = useTableStore()
const notificationStore = useNotificationStore()
const { subscribe, unsubscribe } = useRealtime()

const persons = ref<PersonResponse[]>([])
const loading = ref(false)
const totalElements = ref(0)
const searchQuery = ref('')
const selectedEyeColor = ref<Color | null>(null)
const selectedHairColor = ref<Color | null>(null)

const tableState = computed(() => tableStore.getTableState('persons'))

const dialog = ref({
  show: false,
  mode: 'create' as 'create' | 'edit',
  data: null as PersonResponse | null
})

const detailsDialog = ref({
  show: false,
  data: null as PersonResponse | null
})

const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Height', key: 'height', sortable: true },
  { title: 'Eye Color', key: 'eyeColor', sortable: true },
  { title: 'Hair Color', key: 'hairColor', sortable: true },
  { title: 'Nationality', key: 'nationality', sortable: true },
  { title: 'Location ID', key: 'locationId', sortable: true },
  { title: 'Coordinates ID', key: 'coordinatesId', sortable: true },
  { title: 'Creation Date', key: 'creationDate', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false }
]

const eyeColorOptions = [
  { title: 'Red', value: 'RED' },
  { title: 'Black', value: 'BLACK' },
  { title: 'Blue', value: 'BLUE' },
  { title: 'White', value: 'WHITE' },
  { title: 'Brown', value: 'BROWN' }
]

const hairColorOptions = [
  { title: 'Red', value: 'RED' },
  { title: 'Black', value: 'BLACK' },
  { title: 'Blue', value: 'BLUE' },
  { title: 'White', value: 'WHITE' },
  { title: 'Brown', value: 'BROWN' }
]

const loadPersons = async () => {
  loading.value = true
  try {
    const searchRequest: SearchRequest = {
      page: tableState.value.page,
      size: tableState.value.size,
      sort: tableState.value.sort,
      filters: buildFilters()
    }

    const response = await personsApi.searchPersons(searchRequest)
    persons.value = response.data.content || []
    totalElements.value = response.data.totalElements || 0
  } catch (error) {
    console.error('Error loading persons:', error)
    notificationStore.showNotification('Failed to load persons', 'error')
  } finally {
    loading.value = false
  }
}

const buildFilters = () => {
  const filters = []
  
  if (searchQuery.value) {
    filters.push({ field: 'name', pattern: searchQuery.value })
  }
  
  if (selectedEyeColor.value) {
    filters.push({ field: 'eyeColor', pattern: selectedEyeColor.value })
  }
  
  if (selectedHairColor.value) {
    filters.push({ field: 'hairColor', pattern: selectedHairColor.value })
  }
  
  return filters
}

const onSearch = () => {
  tableStore.setSearch('persons', searchQuery.value)
  loadPersons()
}

const onFilterChange = () => {
  tableStore.setFilters('persons', buildFilters())
  loadPersons()
}

const onPageChange = (page: number) => {
  tableStore.setPage('persons', page - 1)
  loadPersons()
}

const onItemsPerPageChange = (size: number) => {
  tableStore.setSize('persons', size)
  loadPersons()
}

const onSortChange = (sortBy: any[]) => {
  const sort = sortBy.map(item => ({
    field: item.key,
    direction: item.order === 'asc' ? 'ASC' : 'DESC'
  }))
  tableStore.setSort('persons', sort)
  loadPersons()
}

const openCreateDialog = () => {
  dialog.value = {
    show: true,
    mode: 'create',
    data: null
  }
}

const editPerson = (person: PersonResponse) => {
  dialog.value = {
    show: true,
    mode: 'edit',
    data: person
  }
}

const viewPerson = (person: PersonResponse) => {
  detailsDialog.value = {
    show: true,
    data: person
  }
}

const deletePerson = async (person: PersonResponse) => {
  if (confirm(`Are you sure you want to delete ${person.name}?`)) {
    try {
      await personsApi.deletePerson(person.id!)
      notificationStore.showNotification('Person deleted successfully', 'success')
      loadPersons()
    } catch (error) {
      console.error('Error deleting person:', error)
      notificationStore.showNotification('Failed to delete person', 'error')
    }
  }
}

const onPersonSave = () => {
  closeDialog()
  loadPersons()
}

const closeDialog = () => {
  dialog.value.show = false
}

const closeDetailsDialog = () => {
  detailsDialog.value.show = false
}

const getColorChipColor = (color: Color) => {
  const colorMap: Record<Color, string> = {
    RED: 'red',
    BLACK: 'grey darken-3',
    BLUE: 'blue',
    WHITE: 'grey lighten-2',
    BROWN: 'brown'
  }
  return colorMap[color] || 'grey'
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

const handleRealtimeUpdate = (event: any) => {
  if (event.entity === 'PERSON') {
    loadPersons()
  }
}

onMounted(() => {
  loadPersons()
  subscribe('PERSON', handleRealtimeUpdate)
})

watch(() => tableState.value, () => {
  loadPersons()
}, { deep: true })
</script>
