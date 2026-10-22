import type { StatusOportunidade } from '../types/oportunidade'

export const STATUS_OPORTUNIDADE_OPTIONS: { label: string; value: StatusOportunidade }[] = [
  { label: 'Triagem', value: 'TRIAGEM' },
  { label: 'Em avaliação', value: 'EM_AVALIACAO' },
  { label: 'Aprovada', value: 'APROVADA' },
  { label: 'Rejeitada', value: 'REJEITADA' },
  { label: 'Cancelada', value: 'CANCELADA' },
]

export const STATUS_OPORTUNIDADE_SEVERITY: Record<StatusOportunidade, 'success' | 'info' | 'warning' | 'danger' | 'secondary'> = {
  TRIAGEM: 'info',
  EM_AVALIACAO: 'warning',
  APROVADA: 'success',
  REJEITADA: 'danger',
  CANCELADA: 'secondary',
}

export const ORIGEM_OPORTUNIDADE_OPTIONS = [
  { label: 'Demanda interna', value: 'DEMANDA_INTERNA' },
  { label: 'Prospecção', value: 'PROSPECCAO' },
  { label: 'Indicação de parceiro', value: 'INDICACAO_PARCEIRO' },
  { label: 'Chamamento público', value: 'CHAMAMENTO_PUBLICO' },
  { label: 'Outro', value: 'OUTRO' },
]

export const AVALIACAO_CRITERIOS = [
  { key: 'alinhamentoEstrategico', label: 'Alinhamento estratégico' },
  { key: 'potencialInovacao', label: 'Potencial de inovação' },
  { key: 'retornoFinanceiro', label: 'Retorno financeiro' },
  { key: 'impactoInstitucional', label: 'Impacto institucional' },
  { key: 'viabilidadeTecnica', label: 'Viabilidade técnica' },
] as const

export function labelStatusOportunidade(status?: string | null) {
  return STATUS_OPORTUNIDADE_OPTIONS.find((item) => item.value === status)?.label ?? status ?? '—'
}

export function labelOrigemOportunidade(origem?: string | null) {
  return ORIGEM_OPORTUNIDADE_OPTIONS.find((item) => item.value === origem)?.label ?? origem ?? '—'
}
