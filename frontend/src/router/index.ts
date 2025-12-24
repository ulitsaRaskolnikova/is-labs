import { createRouter, createWebHistory } from 'vue-router'
import PersonTable from '@/views/PersonTable.vue'
import LocationTable from '@/views/LocationTable.vue'
import CoordinatesTable from '@/views/CoordinatesTable.vue'
import SpecialOperations from '@/views/SpecialOperations.vue'
import FileUpload from '@/views/FileUpload.vue'
import ImportHistory from '@/views/ImportHistory.vue'

const routes = [
  {
    path: '/',
    redirect: '/persons'
  },
  {
    path: '/persons',
    name: 'Persons',
    component: PersonTable
  },
  {
    path: '/locations',
    name: 'Locations',
    component: LocationTable
  },
  {
    path: '/coordinates',
    name: 'Coordinates',
    component: CoordinatesTable
  },
  {
    path: '/special-operations',
    name: 'SpecialOperations',
    component: SpecialOperations
  },
  {
    path: '/file-upload',
    name: 'FileUpload',
    component: FileUpload
  },
  {
    path: '/import-history',
    name: 'ImportHistory',
    component: ImportHistory
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
