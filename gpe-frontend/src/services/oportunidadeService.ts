import { api } from './api'
import type {
  AvaliacaoPayload,
  Oportunidade,
  OportunidadePayload,
  WorkflowPayload,
} from '../types/oportunidade'
import type { Parceiro } from '../types/parceiro'

export const oportunidadeService = {
  listar: () => api.get<Oportunidade[]>('/oportunidades').then((response) => response.data),
  listarParceiros: () => api.get<Parceiro[]>('/parceiros').then((response) => response.data),
  criar: (dados: OportunidadePayload) =>
    api.post<Oportunidade>('/oportunidades', dados).then((response) => response.data),
  avaliar: (id: string, dados: AvaliacaoPayload) =>
    api.post(`/oportunidades/${id}/avaliacoes`, dados).then((response) => response.data),
  enviarParaAprovacao: (id: string, dados: WorkflowPayload) =>
    api.post<Oportunidade>(`/oportunidades/${id}/enviar-aprovacao`, dados).then((response) => response.data),
  aprovar: (id: string, dados: WorkflowPayload) =>
    api.post<Oportunidade>(`/oportunidades/${id}/aprovar`, dados).then((response) => response.data),
  reprovar: (id: string, dados: WorkflowPayload) =>
    api.post<Oportunidade>(`/oportunidades/${id}/reprovar`, dados).then((response) => response.data),
}
