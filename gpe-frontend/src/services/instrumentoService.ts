import { api } from './api'
import type {
  InstrumentoJuridico,
  InstrumentoAlerta,
  InstrumentoPayload,
  OportunidadeResumo,
  ProrrogacaoInstrumento,
} from '../types/instrumento'
import type { Parceiro } from '../types/parceiro'

export const instrumentoService = {
  listar: () => api.get<InstrumentoJuridico[]>('/instrumentos').then((response) => response.data),
  listarAlertasVencimento: () =>
    api.get<InstrumentoAlerta[]>('/instrumentos/alertas/vencimento').then((response) => response.data),
  criar: (dados: InstrumentoPayload) =>
    api.post<InstrumentoJuridico>('/instrumentos', dados).then((response) => response.data),
  listarProrrogacoes: (id: string) =>
    api.get<ProrrogacaoInstrumento[]>(`/instrumentos/${id}/prorrogacoes`).then((response) => response.data),
  prorrogar: (id: string, novaDataFim: string, justificativa: string) =>
    api.post<ProrrogacaoInstrumento>(`/instrumentos/${id}/prorrogacoes`, {
      novaDataFim,
      justificativa: justificativa.trim() || null,
    }).then((response) => response.data),
  listarParceiros: () => api.get<Parceiro[]>('/parceiros').then((response) => response.data),
  listarOportunidades: () =>
    api.get<OportunidadeResumo[]>('/oportunidades').then((response) => response.data),
}
