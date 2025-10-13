export * from '../api'

export interface TableState {
  page: number
  size: number
  sort: Array<{ field: string; direction: 'ASC' | 'DESC' }>
  filters: Array<{ field: string; pattern: string }>
  search: string
}

export interface NotificationState {
  show: boolean
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
}

export interface DialogState {
  show: boolean
  title: string
  mode: 'create' | 'edit' | 'view'
  data?: any
}

export const validationRules = {
  required: (value: any) =>
      (value !== null && value !== undefined && value !== '') || 'This field is required',
  email: (value: string) => {
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return pattern.test(value) || 'Invalid email format'
  },
  minLength: (min: number) => (value: string) =>
      (value && value.length >= min) || `Minimum length is ${min} characters`,
  maxLength: (max: number) => (value: string) =>
      (value && value.length <= max) || `Maximum length is ${max} characters`,
  positiveNumber: (value: number) =>
      (value !== null && value !== undefined && value > 0) || 'Must be a positive number',
  nonNegativeNumber: (value: number) =>
      (value !== null && value !== undefined && value >= 0) || 'Must be a non-negative number'
}

