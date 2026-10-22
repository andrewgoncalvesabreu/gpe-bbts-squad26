import { api } from './api'
import type {
  AvaliacaoOportunidade,
  AvaliacaoPayload,
  Oportunidade,
  OportunidadePayload,
  StatusOportunidade,
} from '../types/oportunidade'

export const oportunidadeService = {
  listar: (parceiroId?: string) =>
    api.get<Oportunidade[]>('/oportunidades', { params: parceiroId ? { parceiroId } : undefined }).then((r) => r.data),

  buscarPorId: (id: string) => api.get<Oportunidade>(`/oportunidades/${id}`).then((r) => r.data),

  criar: (dados: OportunidadePayload) => api.post<Oportunidade>('/oportunidades', dados).then((r) => r.data),

  atualizar: (id: string, dados: OportunidadePayload) =>
    api.put<Oportunidade>(`/oportunidades/${id}`, dados).then((r) => r.data),

  excluir: (id: string) => api.delete<void>(`/oportunidades/${id}`).then(() => undefined),

  listarAvaliacoes: (id: string) =>
    api.get<AvaliacaoOportunidade[]>(`/oportunidades/${id}/avaliacoes`).then((r) => r.data),

  avaliar: (id: string, dados: AvaliacaoPayload) =>
    api.post<AvaliacaoOportunidade>(`/oportunidades/${id}/avaliacoes`, dados).then((r) => r.data),

  alterarStatus: (id: string, novoStatus: StatusOportunidade) =>
    api.patch<Oportunidade>(`/oportunidades/${id}/status`, { novoStatus }).then((r) => r.data),
}
