<template>
  <v-app>
    <v-app-bar color="primary" dark>
      <v-app-bar-title>Person Management System</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-btn
        v-for="item in menuItems"
        :key="item.name"
        :to="item.path"
        text
        class="mx-2"
      >
        <v-icon left>{{ item.icon }}</v-icon>
        {{ item.name }}
      </v-btn>
    </v-app-bar>

    <v-main>
      <router-view />
    </v-main>

    <v-footer app>
      <span>&copy; 2024 Person Management System</span>
    </v-footer>

    <NotificationSnackbar />
  </v-app>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import NotificationSnackbar from '@/components/NotificationSnackbar.vue'

const menuItems = ref([
  { name: 'Persons', path: '/persons', icon: 'mdi-account' },
  { name: 'Locations', path: '/locations', icon: 'mdi-map-marker' },
  { name: 'Coordinates', path: '/coordinates', icon: 'mdi-crosshairs-gps' },
  { name: 'Special Operations', path: '/special-operations', icon: 'mdi-cog' }
])
</script>
