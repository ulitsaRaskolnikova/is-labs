import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { NotificationState } from '@/types'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<NotificationState[]>([])

  const showNotification = (message: string, type: NotificationState['type'] = 'info') => {
    const notification: NotificationState = {
      show: true,
      message,
      type
    }
    
    notifications.value.push(notification)
    
    setTimeout(() => {
      removeNotification(notification)
    }, 5000)
  }

  const removeNotification = (notification: NotificationState) => {
    const index = notifications.value.indexOf(notification)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  const clearAll = () => {
    notifications.value = []
  }

  return {
    notifications,
    showNotification,
    removeNotification,
    clearAll
  }
})
