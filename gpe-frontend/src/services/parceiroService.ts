import { api } from './api'
import type { Parceiro, ParceiroPayload } from '../types/parceiro'

export const parceiroService = {
  listar: () => api.get<Parceiro[]>('/parceiros').then((r) => r.data),
  buscarPorId: (id: string) => api.get<Parceiro>(`/parceiros/${id}`).then((r) => r.data),
  criar: (dados: ParceiroPayload, responsavelMatricula: string) =>
    api.post<Parceiro>('/parceiros', dados, {
      headers: { 'X-Responsavel-Matricula': responsavelMatricula },
    }).then((r) => r.data),
  atualizar: (id: string, dados: ParceiroPayload, responsavelMatricula: string) =>
    api.put<Parceiro>(`/parceiros/${id}`, dados, {
      headers: { 'X-Responsavel-Matricula': responsavelMatricula },
    }).then((r) => r.data),
  excluir: (id: string, responsavelMatricula: string) =>
    api.delete<void>(`/parceiros/${id}`, {
      headers: { 'X-Responsavel-Matricula': responsavelMatricula },
    }).then(() => undefined),
}
