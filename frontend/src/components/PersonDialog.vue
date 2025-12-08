<template>
  <v-dialog v-model="dialog" max-width="800px" persistent>
    <v-card>
      <v-card-title>
        <v-icon class="mr-2">mdi-account</v-icon>
        {{ mode === 'create' ? 'Create New Person' : 'Edit Person' }}
      </v-card-title>

      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="formData.name"
                label="Name *"
                :rules="[validationRules.required]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model.number="formData.height"
                label="Height (cm)"
                type="number"
                :rules="[validationRules.positiveNumber]"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.eyeColor"
                :items="eyeColorOptions"
                label="Eye Color *"
                :rules="[validationRules.required]"
                required
              ></v-select>
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.hairColor"
                :items="hairColorOptions"
                label="Hair Color *"
                :rules="[validationRules.required]"
                required
              ></v-select>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.nationality"
                :items="nationalityOptions"
                label="Nationality"
                clearable
              ></v-select>
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                  v-model="formData.locationId"
                  :items="locationOptions"
                  label="Location ID"
                  item-title="label"
                  item-value="value"
                  :loading="loadingLocations"
                  clearable
                  @update:menu="onLocationMenuToggle"
              ></v-select>
            </v-col>
          </v-row>

          <v-col cols="12">
            <v-select
                v-model="formData.coordinatesId"
                :items="coordinatesOptions"
                label="Coordinates ID *"
                item-title="label"
                item-value="value"
                :loading="loadingCoordinates"
                :rules="[validationRules.required]"
                required
                @update:menu="onCoordinatesMenuToggle"
            ></v-select>
          </v-col>

          <!-- Quick Create Buttons -->
          <v-divider class="my-4"></v-divider>
          <v-row>
            <v-col cols="12">
              <h4>Quick Create Related Objects</h4>
            </v-col>
            <v-col cols="12" md="6">
              <v-btn
                color="primary"
                variant="outlined"
                @click="openLocationDialog"
                prepend-icon="mdi-map-marker-plus"
                block
              >
                Create New Location
              </v-btn>
            </v-col>
            <v-col cols="12" md="6">
              <v-btn
                color="primary"
                variant="outlined"
                @click="openCoordinatesDialog"
                prepend-icon="mdi-crosshairs-gps"
                block
              >
                Create New Coordinates
              </v-btn>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog">Cancel</v-btn>
        <v-btn
          color="primary"
          @click="savePerson"
          :disabled="!valid || loading"
          :loading="loading"
        >
          {{ mode === 'create' ? 'Create' : 'Update' }}
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Location Dialog -->
    <LocationDialog
      v-model="locationDialog.show"
      @save="onLocationSave"
      @close="closeLocationDialog"
    />

    <!-- Coordinates Dialog -->
    <CoordinatesDialog
      v-model="coordinatesDialog.show"
      @save="onCoordinatesSave"
      @close="closeCoordinatesDialog"
    />
  </v-dialog>
</template>

<script setup lang="ts">
import {locationsApi, coordinatesApi, personsApi} from '@/services/api'
import {computed, ref, watch} from "vue";
import {PersonRequest, PersonResponse, validationRules} from "@/types";
import {useNotificationStore} from "@/stores/notification.ts";

const locationOptions = ref<{ label: string; value: number }[]>([])
const coordinatesOptions = ref<{ label: string; value: number }[]>([])
const loadingLocations = ref(false)
const loadingCoordinates = ref(false)

const onLocationMenuToggle = async (isOpen: boolean) => {
  if (!isOpen || locationOptions.value.length > 0) return

  loadingLocations.value = true
  try {
    const response = await locationsApi.searchLocations({
      page: 0,
      size: 100,
      sort: [],
      filters: []
    })
    const content = response.data.content || []
    locationOptions.value = content.map((loc: any) => ({
      label: `ID ${loc.id} — (${loc.x}, ${loc.y}, ${loc.z})`,
      value: loc.id
    }))
  } catch (error) {
    console.error('Failed to load locations:', error)
  } finally {
    loadingLocations.value = false
  }
}

const onCoordinatesMenuToggle = async (isOpen: boolean) => {
  if (!isOpen || coordinatesOptions.value.length > 0) return

  loadingCoordinates.value = true
  try {
    const response = await coordinatesApi.searchCoordinates({
      page: 0,
      size: 100,
      sort: [],
      filters: []
    })
    const content = response.data.content || []
    coordinatesOptions.value = content.map((c: any) => ({
      label: `ID ${c.id} — (${c.x}, ${c.y})`,
      value: c.id
    }))
  } catch (error) {
    console.error('Failed to load coordinates:', error)
  } finally {
    loadingCoordinates.value = false
  }
}


interface Props {
  modelValue: boolean
  mode: 'create' | 'edit'
  person?: PersonResponse | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'save'): void
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const notificationStore = useNotificationStore()

const dialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const form = ref()
const valid = ref(false)
const loading = ref(false)

const formData = ref<PersonRequest>({
  name: '',
  height: null,
  eyeColor: 'BLUE',
  hairColor: 'BROWN',
  nationality: undefined,
  locationId: undefined,
  coordinatesId: 0
})

const locationDialog = ref({ show: false })
const coordinatesDialog = ref({ show: false })

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

const nationalityOptions = [
  { title: 'Russia', value: 'RUSSIA' },
  { title: 'United Kingdom', value: 'UNITED_KINGDOM' },
  { title: 'Spain', value: 'SPAIN' },
  { title: 'Italy', value: 'ITALY' },
  { title: 'South Korea', value: 'SOUTH_KOREA' }
]

const resetForm = () => {
  formData.value = {
    name: '',
    height: null,
    eyeColor: 'BLUE',
    hairColor: 'BROWN',
    nationality: undefined,
    locationId: undefined,
    coordinatesId: 0
  }
  if (form.value) {
    form.value.resetValidation()
  }
}

const loadPersonData = () => {
  if (props.person) {
    formData.value = {
      name: props.person.name,
      height: props.person.height,
      eyeColor: props.person.eyeColor,
      hairColor: props.person.hairColor,
      nationality: props.person.nationality,
      locationId: props.person.locationId,
      coordinatesId: props.person.coordinatesId
    }
  } else {
    resetForm()
  }
}

const savePerson = async () => {
  if (!form.value?.validate()) return

  loading.value = true
  try {
    if (props.mode === 'create') {
      await personsApi.createPerson(formData.value)
      notificationStore.showNotification('Person created successfully', 'success')
    } else {
      await personsApi.updatePerson(props.person!.id!, formData.value)
      notificationStore.showNotification('Person updated successfully', 'success')
    }
    emit('save')
  } catch (error: any) {
    console.error('Error creating person:', error)

    const backendMessage = error.response?.data?.message
    const message = backendMessage || 'Failed to create person'

    notificationStore.showNotification(message, 'error')
  } finally {
    loading.value = false
  }
}

const closeDialog = () => {
  emit('close')
  resetForm()
}

const openLocationDialog = () => {
  locationDialog.value.show = true
}

const closeLocationDialog = () => {
  locationDialog.value.show = false
}

const onLocationSave = (locationId: number) => {
  formData.value.locationId = locationId
  closeLocationDialog()
  notificationStore.showNotification('Location created and assigned', 'success')
}

const openCoordinatesDialog = () => {
  coordinatesDialog.value.show = true
}

const closeCoordinatesDialog = () => {
  coordinatesDialog.value.show = false
}

const onCoordinatesSave = (coordinatesId: number) => {
  formData.value.coordinatesId = coordinatesId
  closeCoordinatesDialog()
  notificationStore.showNotification('Coordinates created and assigned', 'success')
}

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    loadPersonData()
  }
})

watch(() => props.person, () => {
  if (props.modelValue) {
    loadPersonData()
  }
})



</script>
