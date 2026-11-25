import { api } from './api'
import type { Auditoria } from '../types/auditoria'

export interface AuditoriaFiltros {
  usuario?: string
  operacao?: string
  entidade?: string
  inicio?: string
  fim?: string
}

export const auditoriaService = {
  consultar: (filtros: AuditoriaFiltros = {}) =>
    api.get<Auditoria[]>('/auditorias', { params: filtros }).then((response) => response.data),
}
