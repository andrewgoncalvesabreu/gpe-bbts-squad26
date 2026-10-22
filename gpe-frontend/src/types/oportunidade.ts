export type StatusOportunidade =
  | 'TRIAGEM'
  | 'EM_AVALIACAO'
  | 'APROVADA'
  | 'REJEITADA'
  | 'CANCELADA'

export interface ParceiroResumo {
  id: string
  razaoSocial: string
  nomeFantasia?: string | null
}

export interface Oportunidade {
  id: string
  titulo: string
  parceiro: ParceiroResumo
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
  problemaDemandado?: string | null
  solucaoProposta?: string | null
  estimativaFinanceira?: number | null
  areaDemandante: string
  responsavelMatricula: string
}

export interface AvaliacaoOportunidade {
  id: string
  alinhamentoEstrategico: number
  potencialInovacao: number
  retornoFinanceiro: number
  impactoInstitucional: number
  viabilidadeTecnica: number
  parecerTecnico?: string | null
  avaliadorMatricula: string
  dataAvaliacao: string
}

export interface AvaliacaoPayload {
  alinhamentoEstrategico: number
  potencialInovacao: number
  retornoFinanceiro: number
  impactoInstitucional: number
  viabilidadeTecnica: number
  parecerTecnico?: string | null
  avaliadorMatricula: string
}
