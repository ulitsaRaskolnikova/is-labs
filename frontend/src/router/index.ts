import { createRouter, createWebHistory } from 'vue-router'
import PersonTable from '@/views/PersonTable.vue'
import LocationTable from '@/views/LocationTable.vue'
import CoordinatesTable from '@/views/CoordinatesTable.vue'
import SpecialOperations from '@/views/SpecialOperations.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
