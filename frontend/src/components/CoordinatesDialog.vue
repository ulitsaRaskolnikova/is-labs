<template>
  <v-dialog v-model="dialog" max-width="500px" persistent>
    <v-card>
      <v-card-title>
        <v-icon class="mr-2">mdi-crosshairs-gps</v-icon>
        Create New Coordinates
      </v-card-title>

      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model.number="formData.x"
                label="X Coordinate *"
                type="number"
                :rules="[validationRules.required]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model.number="formData.y"
                label="Y Coordinate *"
                type="number"
                :rules="[validationRules.required]"
                required
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog">Cancel</v-btn>
        <v-btn
          color="primary"
          @click="saveCoordinates"
          :disabled="!valid || loading"
          :loading="loading"
        >
          Create Coordinates
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { coordinatesApi } from '@/services/api'
import type { CoordinatesRequest } from '@/types'
import { validationRules } from '@/types'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', coordinatesId: number): void
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

const formData = ref<CoordinatesRequest>({
  x: 0,
  y: 0
})

const saveCoordinates = async () => {
  if (!form.value?.validate()) return

  loading.value = true
  try {
    const response = await coordinatesApi.createCoordinates(formData.value)
    emit('save', response.data.id!)
    notificationStore.showNotification('Coordinates created successfully', 'success')
  } catch (error: any) {
    console.error('Error creating coordinates:', error)

    const backendMessage = error.response?.data?.message
    const message = backendMessage || 'Failed to create coordinates'

    notificationStore.showNotification(message, 'error')
  } finally {
    loading.value = false
  }
}

const closeDialog = () => {
  emit('close')
  formData.value = { x: 0, y: 0 }
  if (form.value) {
    form.value.resetValidation()
  }
}
</script>
