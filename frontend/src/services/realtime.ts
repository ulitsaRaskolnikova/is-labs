import { ref, onMounted, onUnmounted } from 'vue'

interface RealtimeEvent {
  type: 'CREATE' | 'UPDATE' | 'DELETE'
  entity: 'PERSON' | 'LOCATION' | 'COORDINATES'
  data: any
}

class RealtimeService {
  private eventSource: EventSource | null = null
  private listeners: Map<string, Set<(event: RealtimeEvent) => void>> = new Map()
  private isConnected = ref(false)

  connect() {
    if (this.eventSource) {
      return
    }

    try {
      this.eventSource = new EventSource('/api/events')
      
      this.eventSource.onopen = () => {
        console.log('Realtime connection established')
        this.isConnected.value = true
      }

      this.eventSource.onmessage = (event) => {
        try {
          const realtimeEvent: RealtimeEvent = JSON.parse(event.data)
          this.notifyListeners(realtimeEvent)
        } catch (error) {
          console.error('Error parsing realtime event:', error)
        }
      }

      this.eventSource.onerror = (error) => {
        console.warn('Realtime connection not available (events endpoint may not exist on backend)')
        this.isConnected.value = false
      }
    } catch (error) {
      console.error('Failed to establish realtime connection:', error)
    }
  }

  disconnect() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
      this.isConnected.value = false
    }
  }

  subscribe(entity: string, callback: (event: RealtimeEvent) => void) {
    if (!this.listeners.has(entity)) {
      this.listeners.set(entity, new Set())
    }
    this.listeners.get(entity)!.add(callback)

    if (!this.eventSource) {
      this.connect()
    }
  }

  unsubscribe(entity: string, callback: (event: RealtimeEvent) => void) {
    const entityListeners = this.listeners.get(entity)
    if (entityListeners) {
      entityListeners.delete(callback)
      if (entityListeners.size === 0) {
        this.listeners.delete(entity)
      }
    }
  }

  private notifyListeners(event: RealtimeEvent) {
    const entityListeners = this.listeners.get(event.entity)
    if (entityListeners) {
      entityListeners.forEach(callback => callback(event))
    }
  }

  getConnectionStatus() {
    return this.isConnected
  }
}

export const realtimeService = new RealtimeService()

export function useRealtime() {
  const isConnected = realtimeService.getConnectionStatus()

  onMounted(() => {
    realtimeService.connect()
  })

  onUnmounted(() => {
    realtimeService.disconnect()
  })

  return {
    isConnected,
    subscribe: realtimeService.subscribe.bind(realtimeService),
    unsubscribe: realtimeService.unsubscribe.bind(realtimeService)
  }
}
