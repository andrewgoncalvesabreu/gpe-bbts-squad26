import { api } from './api'
import type { Parceiro, ParceiroPayload } from '../types/parceiro'

export const parceiroService = {
  listar: () => api.get<Parceiro[]>('/parceiros').then((r) => r.data),
  buscarPorId: (id: string) => api.get<Parceiro>(`/parceiros/${id}`).then((r) => r.data),
  criar: (dados: ParceiroPayload) => api.post<Parceiro>('/parceiros', dados).then((r) => r.data),
  atualizar: (id: string, dados: ParceiroPayload) =>
    api.put<Parceiro>(`/parceiros/${id}`, dados).then((r) => r.data),
  excluir: (id: string) => api.delete<void>(`/parceiros/${id}`).then(() => undefined),
}
