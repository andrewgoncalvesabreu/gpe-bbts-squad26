import axios from 'axios'
import { keycloak } from '../auth/keycloak'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api/v1',
  timeout: 15000,
})

api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    await keycloak.updateToken(30)

    if (keycloak.token) {
      config.headers.Authorization = `Bearer ${keycloak.token}`
    }
  }

  const body = config.data

  if (body instanceof FormData) {
    if (config.headers) {
      delete config.headers['Content-Type']
      delete config.headers['content-type']
    }
  } else if (body !== undefined && config.headers) {
    config.headers['Content-Type'] = 'application/json'
  }

  return config
})
