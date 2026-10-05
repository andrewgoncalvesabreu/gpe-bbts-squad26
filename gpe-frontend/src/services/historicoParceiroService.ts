import { api } from './api'
import type { HistoricoParceiro, HistoricoPayload } from '../types/parceiro'

export const historicoParceiroService = {
  listar: (parceiroId: string) =>
    api.get<HistoricoParceiro[]>(`/parceiros/${parceiroId}/historico`).then((r) => r.data),
  adicionar: (parceiroId: string, dados: HistoricoPayload) =>
    api.post<HistoricoParceiro>(`/parceiros/${parceiroId}/historico`, dados).then((r) => r.data),
}
