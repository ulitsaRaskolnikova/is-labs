import { PersonsApi, LocationsApi, CoordinatesApi, OperationsApi } from '../api.js'
import { Configuration } from '../configuration.js'
import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

apiClient.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`)
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('Response error:', error.response?.data || error.message)
    
    if (error.response?.status === 400) {
      console.error('Validation error:', error.response.data)
    } else if (error.response?.status === 404) {
      console.error('Resource not found')
    } else if (error.response?.status >= 500) {
      console.error('Server error')
    }
    
    return Promise.reject(error)
  }
)

const configuration = new Configuration({
  basePath: '/api',
  baseOptions: {
    headers: {
      'Content-Type': 'application/json',
    },
  },
})

export const personsApi = new PersonsApi(configuration, '/api', apiClient)
export const locationsApi = new LocationsApi(configuration, '/api', apiClient)
export const coordinatesApi = new CoordinatesApi(configuration, '/api', apiClient)
export const operationsApi = new OperationsApi(configuration, '/api', apiClient)

export default apiClient
