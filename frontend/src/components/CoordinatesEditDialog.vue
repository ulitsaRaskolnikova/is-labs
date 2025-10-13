<template>
  <v-dialog v-model="dialog" max-width="500px" persistent>
    <v-card>
      <v-card-title>
        <v-icon class="mr-2">mdi-crosshairs-gps</v-icon>
        {{ mode === 'create' ? 'Create New Coordinates' : 'Edit Coordinates' }}
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
          {{ mode === 'create' ? 'Create' : 'Update' }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { coordinatesApi } from '@/services/api'
import type { CoordinatesRequest, CoordinatesResponse } from '@/types'
import { validationRules } from '@/types'

interface Props {
  modelValue: boolean
  mode: 'create' | 'edit'
  coordinates?: CoordinatesResponse | null
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

const formData = ref<CoordinatesRequest>({
  x: 0,
  y: 0
})

const resetForm = () => {
  formData.value = { x: 0, y: 0 }
  if (form.value) {
    form.value.resetValidation()
  }
}

const loadCoordinatesData = () => {
  if (props.coordinates) {
    formData.value = {
      x: props.coordinates.x,
      y: props.coordinates.y
    }
  } else {
    resetForm()
  }
}

const saveCoordinates = async () => {
  if (!form.value?.validate()) return

  loading.value = true
  try {
    if (props.mode === 'create') {
      await coordinatesApi.createCoordinates(formData.value)
      notificationStore.showNotification('Coordinates created successfully', 'success')
    } else {
      await coordinatesApi.updateCoordinates(props.coordinates!.id!, formData.value)
      notificationStore.showNotification('Coordinates updated successfully', 'success')
    }
    emit('save')
  } catch (error: any) {
    console.error('Error editing coordinates:', error)

    const backendMessage = error.response?.data?.message
    const message = backendMessage || 'Failed to create coordinates'

    notificationStore.showNotification(message, 'error')
  } finally {
    loading.value = false
  }
}

const closeDialog = () => {
  emit('close')
  resetForm()
}

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    loadCoordinatesData()
  }
})

watch(() => props.coordinates, () => {
  if (props.modelValue) {
    loadCoordinatesData()
  }
})
</script>
