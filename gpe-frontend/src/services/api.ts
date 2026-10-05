import axios from 'axios'

// Em dev, o Vite faz proxy de /api -> http://localhost:8081 (veja vite.config.ts).
// Em produção, defina VITE_API_URL (ex.: https://gpe.bbts.com.br/api/v1).
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})
