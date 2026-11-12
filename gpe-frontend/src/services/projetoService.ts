import { api } from './api'
import type {
  InstrumentoResumo,
  Parceria,
  ParceriaPayload,
  ParceiroResumo,
  PlanoTrabalho,
  PlanoTrabalhoPayload,
  Projeto,
  ProjetoPayload,
} from '../types/projeto'

export const projetoService = {
  listarParceiros: () => api.get<ParceiroResumo[]>('/parceiros').then((response) => response.data),
  listarInstrumentos: () => api.get<InstrumentoResumo[]>('/instrumentos').then((response) => response.data),
  listarParcerias: (parceiroId?: string) =>
    api.get<Parceria[]>('/parcerias', { params: parceiroId ? { parceiroId } : undefined })
      .then((response) => response.data),
  criarParceria: (payload: ParceriaPayload) =>
    api.post<Parceria>('/parcerias', payload).then((response) => response.data),
  listar: () => api.get<Projeto[]>('/projetos').then((response) => response.data),
  criar: (payload: ProjetoPayload) =>
    api.post<Projeto>('/projetos', payload).then((response) => response.data),
  excluir: (id: string) => api.delete<void>(`/projetos/${id}`).then(() => undefined),
  buscarPlano: (projectId: string) =>
    api.get<PlanoTrabalho>(`/projetos/${projectId}/plano-trabalho`).then((response) => response.data),
  criarPlano: (projectId: string, payload: PlanoTrabalhoPayload) =>
    api.post<PlanoTrabalho>(`/projetos/${projectId}/plano-trabalho`, payload).then((response) => response.data),
  atualizarPlano: (projectId: string, payload: PlanoTrabalhoPayload) =>
    api.put<PlanoTrabalho>(`/projetos/${projectId}/plano-trabalho`, payload).then((response) => response.data),
  excluirPlano: (projectId: string) =>
    api.delete<void>(`/projetos/${projectId}/plano-trabalho`).then(() => undefined),
}
