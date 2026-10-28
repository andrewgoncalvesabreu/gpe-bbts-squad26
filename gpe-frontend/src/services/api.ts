import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api/v1',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
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
