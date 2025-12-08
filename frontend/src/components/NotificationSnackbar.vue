<template>
  <v-snackbar
    v-for="(notification, index) in notifications"
    :key="index"
    v-model="notification.show"
    :color="notification.type"
    :timeout="5000"
    location="top right"
    @update:model-value="removeNotification(notification)"
  >
    {{ notification.message }}
    <template v-slot:actions>
      <v-btn
        icon="mdi-close"
        variant="text"
        @click="removeNotification(notification)"
      ></v-btn>
    </template>
  </v-snackbar>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()
const { notifications } = storeToRefs(notificationStore)

const removeNotification = (notification: any) => {
  notificationStore.removeNotification(notification)
}
</script>
