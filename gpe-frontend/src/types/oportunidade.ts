import type { Parceiro } from './parceiro'

export type StatusOportunidade =
  | 'TRIAGEM'
  | 'EM_AVALIACAO'
  | 'EM_APROVACAO'
  | 'APROVADA'
  | 'REJEITADA'
  | 'CANCELADA'

export interface Oportunidade {
  id: string
  titulo: string
  parceiro: Pick<Parceiro, 'id' | 'razaoSocial'>
  origem: string
  descricao: string
  objetivos: string
  justificativa: string
  beneficiosEsperados: string
  problemaDemandado?: string | null
  solucaoProposta?: string | null
  estimativaFinanceira?: number | null
  statusWorkflow: StatusOportunidade
  areaDemandante: string
  responsavelMatricula: string
  dataCriacao: string
}

export interface OportunidadePayload {
  titulo: string
  parceiro: { id: string }
  origem: string
  descricao: string
  objetivos: string
  justificativa: string
  beneficiosEsperados: string
  areaDemandante: string
  responsavelMatricula: string
}

export interface AvaliacaoPayload {
  alinhamentoEstrategico: number
  potencialInovacao: number
  retornoFinanceiro: number
  impactoInstitucional: number
  viabilidadeTecnica: number
  parecerTecnico?: string
  avaliadorMatricula: string
}

export interface WorkflowPayload {
  responsavelMatricula: string
  justificativa?: string
}
