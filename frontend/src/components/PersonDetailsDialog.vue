<template>
  <v-dialog v-model="dialog" max-width="600px">
    <v-card>
      <v-card-title class="d-flex align-center">
        <v-icon class="mr-2">mdi-account-details</v-icon>
        Person Details
        <v-spacer></v-spacer>
        <v-btn icon="mdi-close" variant="text" @click="closeDialog"></v-btn>
      </v-card-title>

      <v-card-text v-if="person">
        <v-row>
          <v-col cols="12" md="6">
            <v-card variant="outlined" class="pa-4">
              <v-card-title class="text-h6">Basic Information</v-card-title>
              <v-list density="compact">
                <v-list-item>
                  <v-list-item-title>ID</v-list-item-title>
                  <v-list-item-subtitle>{{ person.id }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Name</v-list-item-title>
                  <v-list-item-subtitle>{{ person.name }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Height</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ person.height ? `${person.height} cm` : 'Not specified' }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Creation Date</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(person.creationDate) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>

          <v-col cols="12" md="6">
            <v-card variant="outlined" class="pa-4">
              <v-card-title class="text-h6">Physical Characteristics</v-card-title>
              <v-list density="compact">
                <v-list-item>
                  <v-list-item-title>Eye Color</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip :color="getColorChipColor(person.eyeColor)" size="small">
                      {{ person.eyeColor }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Hair Color</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip :color="getColorChipColor(person.hairColor)" size="small">
                      {{ person.hairColor }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Nationality</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip v-if="person.nationality" color="blue" size="small">
                      {{ person.nationality }}
                    </v-chip>
                    <span v-else>Not specified</span>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>
        </v-row>

        <v-row class="mt-4">
          <v-col cols="12">
            <v-card variant="outlined" class="pa-4">
              <v-card-title class="text-h6">Location Information</v-card-title>
              <v-list density="compact">
                <v-list-item>
                  <v-list-item-title>Location ID</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ person.locationId || 'Not assigned' }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Coordinates ID</v-list-item-title>
                  <v-list-item-subtitle>{{ person.coordinatesId }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>
        </v-row>

        <!-- Related Objects Information -->
        <v-row class="mt-4" v-if="relatedData">
          <v-col cols="12" md="6" v-if="relatedData.location">
            <v-card variant="outlined" class="pa-4">
              <v-card-title class="text-h6">Location Details</v-card-title>
              <v-list density="compact">
                <v-list-item>
                  <v-list-item-title>X</v-list-item-title>
                  <v-list-item-subtitle>{{ relatedData.location.x }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Y</v-list-item-title>
                  <v-list-item-subtitle>{{ relatedData.location.y }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Z</v-list-item-title>
                  <v-list-item-subtitle>{{ relatedData.location.z }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>

          <v-col cols="12" md="6" v-if="relatedData.coordinates">
            <v-card variant="outlined" class="pa-4">
              <v-card-title class="text-h6">Coordinates Details</v-card-title>
              <v-list density="compact">
                <v-list-item>
                  <v-list-item-title>X</v-list-item-title>
                  <v-list-item-subtitle>{{ relatedData.coordinates.x }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Y</v-list-item-title>
                  <v-list-item-subtitle>{{ relatedData.coordinates.y }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { locationsApi, coordinatesApi } from '@/services/api'
import type { PersonResponse, LocationResponse, CoordinatesResponse, Color } from '@/types'

interface Props {
  modelValue: boolean
  person?: PersonResponse | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const relatedData = ref<{
  location?: LocationResponse
  coordinates?: CoordinatesResponse
} | null>(null)

const loadRelatedData = async () => {
  if (!props.person) return

  try {
    relatedData.value = {}

    if (props.person.coordinatesId) {
      const coordinatesResponse = await coordinatesApi.getCoordinatesById(props.person.coordinatesId)
      relatedData.value.coordinates = coordinatesResponse.data
    }

    if (props.person.locationId) {
      const locationResponse = await locationsApi.getLocationById(props.person.locationId)
      relatedData.value.location = locationResponse.data
    }
  } catch (error: any) {
    console.error('Error creating person:', error)

    const backendMessage = error.response?.data?.message
    const message = backendMessage || 'Failed to create person'

    notificationStore.showNotification(message, 'error')
  }
}

const closeDialog = () => {
  emit('close')
  relatedData.value = null
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

watch(() => props.modelValue, (newValue) => {
  if (newValue && props.person) {
    loadRelatedData()
  }
})

watch(() => props.person, () => {
  if (props.modelValue && props.person) {
    loadRelatedData()
  }
})
</script>
