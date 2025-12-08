<template>
  <v-dialog v-model="dialog" max-width="500px" persistent>
    <v-card>
      <v-card-title>
        <v-icon class="mr-2">mdi-map-marker</v-icon>
        {{ mode === 'create' ? 'Create New Location' : 'Edit Location' }}
      </v-card-title>

      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-row>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="formData.x"
                label="X Coordinate *"
                type="number"
                :rules="[validationRules.required]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="formData.y"
                label="Y Coordinate *"
                type="number"
                :rules="[validationRules.required]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="formData.z"
                label="Z Coordinate *"
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
          @click="saveLocation"
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
import { locationsApi } from '@/services/api'
import type { LocationRequest, LocationResponse } from '@/types'
import { validationRules } from '@/types'

interface Props {
  modelValue: boolean
  mode: 'create' | 'edit'
  location?: LocationResponse | null
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

const formData = ref<LocationRequest>({
  x: 0,
  y: 0,
  z: 0
})

const resetForm = () => {
  formData.value = { x: 0, y: 0, z: 0 }
  if (form.value) {
    form.value.resetValidation()
  }
}

const loadLocationData = () => {
  if (props.location) {
    formData.value = {
      x: props.location.x,
      y: props.location.y,
      z: props.location.z
    }
  } else {
    resetForm()
  }
}

const saveLocation = async () => {
  if (!form.value?.validate()) return

  loading.value = true
  try {
    if (props.mode === 'create') {
      await locationsApi.createLocation(formData.value)
      notificationStore.showNotification('Location created successfully', 'success')
    } else {
      await locationsApi.updateLocation(props.location!.id!, formData.value)
      notificationStore.showNotification('Location updated successfully', 'success')
    }
    emit('save')
  } catch (error: any) {
    console.error('Error editing location:', error)

    const backendMessage = error.response?.data?.message
    const message = backendMessage || 'Failed to edit location'

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
    loadLocationData()
  }
})

watch(() => props.location, () => {
  if (props.modelValue) {
    loadLocationData()
  }
})
</script>
