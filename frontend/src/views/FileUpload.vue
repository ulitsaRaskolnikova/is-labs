<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-file-upload</v-icon>
            File Upload
          </v-card-title>

          <v-card-text>
            <v-form ref="uploadFormRef">
              <v-file-input
                  v-model="uploadForm.file"
                  label="Select CSV or ZIP file"
                  accept=".csv,.zip"
                  prepend-icon="mdi-file"
                  show-size
                  clearable
                  :rules="fileRules"
                  class="mb-4"
              ></v-file-input>
              
              <v-btn
                  color="primary"
                  size="large"
                  @click="uploadFile"
                  :loading="uploadForm.loading"
                  :disabled="!uploadForm.file"
                  block
              >
                <v-icon left>mdi-upload</v-icon>
                Upload File
              </v-btn>
            </v-form>

            <v-card v-if="uploadForm.result" variant="outlined" class="mt-4">
              <v-card-title class="d-flex align-center">
                <v-icon class="mr-2" :color="uploadForm.result.errorCount > 0 ? 'warning' : 'success'">
                  {{ uploadForm.result.errorCount > 0 ? 'mdi-alert' : 'mdi-check-circle' }}
                </v-icon>
                Upload Result
              </v-card-title>
              <v-card-text>
                <v-alert
                    :type="uploadForm.result.errorCount > 0 ? 'warning' : 'success'"
                    :text="uploadForm.result.message"
                    prominent
                    class="mb-4"
                ></v-alert>

                <v-row class="mb-4">
                  <v-col cols="12" md="6">
                    <v-card color="success" variant="flat">
                      <v-card-text>
                        <div class="text-h4">{{ uploadForm.result.processedCount }}</div>
                        <div class="text-body-2">Successfully Processed</div>
                      </v-card-text>
                    </v-card>
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-card color="error" variant="flat">
                      <v-card-text>
                        <div class="text-h4">{{ uploadForm.result.errorCount }}</div>
                        <div class="text-body-2">Errors</div>
                      </v-card-text>
                    </v-card>
                  </v-col>
                </v-row>

                <v-data-table
                    v-if="uploadForm.result.errors && uploadForm.result.errors.length > 0"
                    :headers="errorHeaders"
                    :items="uploadForm.result.errors"
                    :items-per-page="10"
                    class="elevation-1 mt-4"
                >
                  <template v-slot:item.lineNumber="{ item }">
                    <v-chip size="small" color="error">
                      Line {{ item.lineNumber }}
                    </v-chip>
                  </template>
                  <template v-slot:item.field="{ item }">
                    <v-chip size="small" color="warning" v-if="item.field">
                      {{ item.field }}
                    </v-chip>
                    <span v-else class="text-caption">-</span>
                  </template>
                  <template v-slot:item.message="{ item }">
                    <div class="text-body-2">{{ item.message }}</div>
                  </template>
                </v-data-table>
              </v-card-text>
            </v-card>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import apiClient from '@/services/api'

const notificationStore = useNotificationStore()

const uploadForm = ref({
  file: null as File | null,
  loading: false,
  result: null as {
    processedCount: number
    errorCount: number
    errors: Array<{
      lineNumber: number
      fileName: string
      field: string | null
      message: string
      recordData: string | null
    }>
    message: string
  } | null
})

const errorHeaders = [
  { title: 'Line', key: 'lineNumber', sortable: true },
  { title: 'File', key: 'fileName', sortable: true },
  { title: 'Field', key: 'field', sortable: true },
  { title: 'Error', key: 'message', sortable: false }
]

const fileRules = [
  (v: File | null) => {
    if (!v) {
      return 'File is required'
    }
    if (!v.name.endsWith('.csv') && !v.name.endsWith('.zip')) {
      return 'Only CSV and ZIP files are allowed'
    }
    return true
  }
]

const uploadFile = async () => {
  if (!uploadForm.value.file) {
    notificationStore.showNotification('Please select a file', 'error')
    return
  }

  uploadForm.value.loading = true
  uploadForm.value.result = null

  try {
    const formData = new FormData()
    formData.append('file', uploadForm.value.file)

    const response = await apiClient.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    uploadForm.value.result = response.data
    
    if (response.data.errorCount > 0) {
      notificationStore.showNotification(
        `File processed with ${response.data.errorCount} error(s)`,
        'warning'
      )
    } else {
      notificationStore.showNotification('File uploaded successfully', 'success')
      uploadForm.value.file = null
    }
  } catch (error: any) {
    console.error('Error uploading file:', error)
    
    let errorMessage = 'Failed to upload file'
    if (error.response?.status === 503 || error.response?.status === 500) {
      const errorData = error.response?.data
      if (errorData?.error) {
        errorMessage = errorData.error
      } else if (errorData?.message) {
        errorMessage = errorData.message
      } else {
        errorMessage = 'Database is unavailable. Please try again later.'
      }
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error
    } else if (error.message) {
      errorMessage = error.message
    }
    
    uploadForm.value.result = {
      processedCount: 0,
      errorCount: 1,
      errors: [{
        lineNumber: 0,
        fileName: uploadForm.value.file?.name || 'unknown',
        field: null,
        message: errorMessage,
        recordData: null
      }],
      message: errorMessage
    }
    
    notificationStore.showNotification(errorMessage, 'error')
  } finally {
    uploadForm.value.loading = false
  }
}
</script>

