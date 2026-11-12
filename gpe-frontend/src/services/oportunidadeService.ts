import { api } from './api'
import type {
  AvaliacaoOportunidade,
  AvaliacaoPayload,
  Oportunidade,
  OportunidadePayload,
  StatusOportunidade,
  WorkflowPayload,
} from '../types/oportunidade'
import type { Parceiro } from '../types/parceiro'

export const oportunidadeService = {
  listar: (parceiroId?: string) =>
    api.get<Oportunidade[]>('/oportunidades', { params: parceiroId ? { parceiroId } : undefined })
      .then((response) => response.data),
  buscarPorId: (id: string) => api.get<Oportunidade>(`/oportunidades/${id}`).then((response) => response.data),
  listarParceiros: () => api.get<Parceiro[]>('/parceiros').then((response) => response.data),
  criar: (dados: OportunidadePayload) =>
    api.post<Oportunidade>('/oportunidades', dados).then((response) => response.data),
  atualizar: (id: string, dados: OportunidadePayload) =>
    api.put<Oportunidade>(`/oportunidades/${id}`, dados).then((response) => response.data),
  excluir: (id: string) => api.delete<void>(`/oportunidades/${id}`).then(() => undefined),
  listarAvaliacoes: (id: string) =>
    api.get<AvaliacaoOportunidade[]>(`/oportunidades/${id}/avaliacoes`).then((response) => response.data),
  avaliar: (id: string, dados: AvaliacaoPayload) =>
    api.post<AvaliacaoOportunidade>(`/oportunidades/${id}/avaliacoes`, dados).then((response) => response.data),
  enviarParaAprovacao: (id: string, dados: WorkflowPayload) =>
    api.post<Oportunidade>(`/oportunidades/${id}/enviar-aprovacao`, dados).then((response) => response.data),
  aprovar: (id: string, dados: WorkflowPayload) =>
    api.post<Oportunidade>(`/oportunidades/${id}/aprovar`, dados).then((response) => response.data),
  reprovar: (id: string, dados: WorkflowPayload) =>
    api.post<Oportunidade>(`/oportunidades/${id}/reprovar`, dados).then((response) => response.data),
  alterarStatus: (id: string, novoStatus: StatusOportunidade) =>
    api.patch<Oportunidade>(`/oportunidades/${id}/status`, { novoStatus }).then((response) => response.data),
}
