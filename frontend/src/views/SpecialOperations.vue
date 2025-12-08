<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-cog</v-icon>
            Special Operations
          </v-card-title>

          <v-card-text>
            <v-row>

              <!-- Delete by Nationality -->
              <v-col cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-card-title class="text-h6">
                    <v-icon class="mr-2">mdi-delete</v-icon>
                    Delete by Nationality
                  </v-card-title>
                  <v-card-text>
                    <v-form ref="deleteNationalityFormRef">
                      <v-select
                          v-model="deleteNationalityForm.nationality"
                          :items="nationalityOptions"
                          label="Select Nationality"
                          class="mb-4"
                          :rules="[v => !!v || 'Nationality is required']"
                      ></v-select>
                      <v-btn
                          color="error"
                          @click="deleteByNationality"
                          :loading="deleteNationalityForm.loading"
                          :disabled="!deleteNationalityForm.nationality"
                      >
                        Delete Person
                      </v-btn>
                    </v-form>
                  </v-card-text>
                  <v-card-text v-if="deleteNationalityForm.result !== null" class="pt-0">
                    <v-alert
                        :type="deleteNationalityForm.result.success ? 'success' : 'error'"
                        :text="deleteNationalityForm.result.message"
                    ></v-alert>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Sum of Heights -->
              <v-col cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-card-title class="text-h6">
                    <v-icon class="mr-2">mdi-calculator</v-icon>
                    Sum of Heights
                  </v-card-title>
                  <v-card-text>
                    <v-form ref="sumHeightFormRef">
                      <p class="text-body-2 mb-4">
                        Calculate the sum of all height values for all persons.
                      </p>
                      <v-btn
                          color="primary"
                          @click="sumHeight"
                          :loading="sumHeightForm.loading"
                      >
                        Calculate Sum
                      </v-btn>
                    </v-form>
                  </v-card-text>
                  <v-card-text v-if="sumHeightForm.result !== null" class="pt-0">
                    <v-alert
                        :type="sumHeightForm.result.success ? 'success' : 'error'"
                        :text="sumHeightForm.result.message"
                    ></v-alert>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Count by Height -->
              <v-col cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-card-title class="text-h6">
                    <v-icon class="mr-2">mdi-counter</v-icon>
                    Count by Height
                  </v-card-title>
                  <v-card-text>
                    <v-form ref="countHeightFormRef">
                      <v-text-field
                          v-model.number="countHeightForm.height"
                          label="Height (cm)"
                          type="number"
                          class="mb-4"
                          :rules="[v => v > 0 || 'Height must be positive']"
                      ></v-text-field>
                      <v-btn
                          color="primary"
                          @click="countByHeight"
                          :loading="countHeightForm.loading"
                          :disabled="!countHeightForm.height"
                      >
                        Count Persons
                      </v-btn>
                    </v-form>
                  </v-card-text>
                  <v-card-text v-if="countHeightForm.result !== null" class="pt-0">
                    <v-alert
                        :type="countHeightForm.result.success ? 'success' : 'error'"
                        :text="countHeightForm.result.message"
                    ></v-alert>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Eye Color Share -->
              <v-col cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-card-title class="text-h6">
                    <v-icon class="mr-2">mdi-percent</v-icon>
                    Eye Color Share
                  </v-card-title>
                  <v-card-text>
                    <v-form ref="eyeColorShareFormRef">
                      <v-select
                          v-model="eyeColorShareForm.eyeColor"
                          :items="eyeColorOptions"
                          label="Select Eye Color"
                          class="mb-4"
                          :rules="[v => !!v || 'Eye color is required']"
                      ></v-select>
                      <v-btn
                          color="primary"
                          @click="eyeColorShare"
                          :loading="eyeColorShareForm.loading"
                          :disabled="!eyeColorShareForm.eyeColor"
                      >
                        Calculate Share
                      </v-btn>
                    </v-form>
                  </v-card-text>
                  <v-card-text v-if="eyeColorShareForm.result !== null" class="pt-0">
                    <v-alert
                        :type="eyeColorShareForm.result.success ? 'success' : 'error'"
                        :text="eyeColorShareForm.result.message"
                    ></v-alert>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Count by Hair Color & Location -->
              <v-col cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-card-title class="text-h6">
                    <v-icon class="mr-2">mdi-map-marker-multiple</v-icon>
                    Count by Hair Color & Location
                  </v-card-title>
                  <v-card-text>
                    <v-form ref="hairColorLocationFormRef">
                      <v-select
                          v-model="hairColorLocationForm.hairColor"
                          :items="hairColorOptions"
                          label="Select Hair Color"
                          class="mb-4"
                          :rules="[v => !!v || 'Hair color is required']"
                      ></v-select>
                      <v-text-field
                          v-model.number="hairColorLocationForm.locationId"
                          label="Location ID"
                          type="number"
                          class="mb-4"
                          :rules="[v => v > 0 || 'Location ID must be positive']"
                      ></v-text-field>
                      <v-btn
                          color="primary"
                          @click="countByHairColorAndLocation"
                          :loading="hairColorLocationForm.loading"
                          :disabled="!hairColorLocationForm.hairColor || !hairColorLocationForm.locationId"
                      >
                        Count Persons
                      </v-btn>
                    </v-form>
                  </v-card-text>
                  <v-card-text v-if="hairColorLocationForm.result !== null" class="pt-0">
                    <v-alert
                        :type="hairColorLocationForm.result.success ? 'success' : 'error'"
                        :text="hairColorLocationForm.result.message"
                    ></v-alert>
                  </v-card-text>
                </v-card>
              </v-col>

            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { operationsApi } from '@/services/api'
import type { Color, Country } from '@/types'

const notificationStore = useNotificationStore()

const deleteNationalityForm = ref({
  nationality: null as Country | null,
  loading: false,
  result: null as { success: boolean; message: string } | null
})

const sumHeightForm = ref({
  loading: false,
  result: null as { success: boolean; message: string } | null
})

const countHeightForm = ref({
  height: null as number | null,
  loading: false,
  result: null as { success: boolean; message: string } | null
})

const eyeColorShareForm = ref({
  eyeColor: null as Color | null,
  loading: false,
  result: null as { success: boolean; message: string } | null
})

const hairColorLocationForm = ref({
  hairColor: null as Color | null,
  locationId: null as number | null,
  loading: false,
  result: null as { success: boolean; message: string } | null
})

const nationalityOptions = [
  { title: 'Russia', value: 'RUSSIA' },
  { title: 'United Kingdom', value: 'UNITED_KINGDOM' },
  { title: 'Spain', value: 'SPAIN' },
  { title: 'Italy', value: 'ITALY' },
  { title: 'South Korea', value: 'SOUTH_KOREA' }
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

const deleteByNationality = async () => {
  if (!deleteNationalityForm.value.nationality) return

  deleteNationalityForm.value.loading = true
  deleteNationalityForm.value.result = null

  try {
    await operationsApi.deleteByNationality(deleteNationalityForm.value.nationality)
    deleteNationalityForm.value.result = {
      success: true,
      message: `Person with nationality ${deleteNationalityForm.value.nationality} deleted successfully`
    }
    notificationStore.showNotification('Person deleted successfully', 'success')
  } catch (error: any) {
    if (error.response?.status === 404) {
      const message = `Person with nationality "${deleteNationalityForm.value.nationality}" not found`
      deleteNationalityForm.value.result = { success: false, message }
    } else {
      const backendMessage = error.response?.data?.message
      const message = backendMessage || 'Failed to delete person by nationality'
      deleteNationalityForm.value.result = { success: false, message }
    }
  } finally {
    deleteNationalityForm.value.loading = false
  }
}

const sumHeight = async () => {
  sumHeightForm.value.loading = true
  sumHeightForm.value.result = null

  try {
    const response = await operationsApi.sumHeight()
    const value = typeof response.data === 'object' && response.data.value !== undefined
        ? response.data.value
        : response.data
    sumHeightForm.value.result = {
      success: true,
      message: `Sum of all heights: ${value} cm`
    }
  } catch (error) {
    console.error('Error calculating sum height:', error)
    sumHeightForm.value.result = {
      success: false,
      message: 'Failed to calculate sum of heights'
    }
    notificationStore.showNotification('Failed to calculate sum', 'error')
  } finally {
    sumHeightForm.value.loading = false
  }
}

const countByHeight = async () => {
  if (!countHeightForm.value.height) return

  countHeightForm.value.loading = true
  countHeightForm.value.result = null

  try {
    const response = await operationsApi.countByHeight(countHeightForm.value.height)
    const value = typeof response.data === 'object' && response.data.value !== undefined
        ? response.data.value
        : response.data
    countHeightForm.value.result = {
      success: true,
      message: `Found ${value} persons with height ${countHeightForm.value.height} cm`
    }
  } catch (error) {
    console.error('Error counting by height:', error)
    countHeightForm.value.result = {
      success: false,
      message: 'Failed to count persons by height'
    }
    notificationStore.showNotification('Failed to count by height', 'error')
  } finally {
    countHeightForm.value.loading = false
  }
}

const eyeColorShare = async () => {
  if (!eyeColorShareForm.value.eyeColor) return

  eyeColorShareForm.value.loading = true
  eyeColorShareForm.value.result = null

  try {
    const response = await operationsApi.eyeColorShare(eyeColorShareForm.value.eyeColor)
    const value = typeof response.data === 'object' && response.data.value !== undefined
        ? response.data.value
        : response.data
    eyeColorShareForm.value.result = {
      success: true,
      message: `Share of persons with ${eyeColorShareForm.value.eyeColor} eyes: ${value.toFixed(2)}%`
    }
  } catch (error) {
    console.error('Error calculating eye color share:', error)
    eyeColorShareForm.value.result = {
      success: false,
      message: 'There are no people with such color'
    }
  } finally {
    eyeColorShareForm.value.loading = false
  }
}

const countByHairColorAndLocation = async () => {
  if (!hairColorLocationForm.value.hairColor || !hairColorLocationForm.value.locationId) return

  hairColorLocationForm.value.loading = true
  hairColorLocationForm.value.result = null

  try {
    const response = await operationsApi.countByHairColorAndLocation(
        hairColorLocationForm.value.hairColor,
        hairColorLocationForm.value.locationId
    )
    const value = typeof response.data === 'object' && response.data.value !== undefined
        ? response.data.value
        : response.data
    hairColorLocationForm.value.result = {
      success: true,
      message: `Found ${value} persons with ${hairColorLocationForm.value.hairColor} hair in location ${hairColorLocationForm.value.locationId}`
    }
  } catch (error) {
    console.error('Error counting by hair color and location:', error)
    hairColorLocationForm.value.result = {
      success: false,
      message: 'Failed to count persons by hair color and location'
    }
    notificationStore.showNotification('Failed to count by hair color and location', 'error')
  } finally {
    hairColorLocationForm.value.loading = false
  }
}
</script>
