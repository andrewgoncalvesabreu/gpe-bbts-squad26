// Espelha as entidades do backend (Parceiro.java / HistoricoParceiro.java)

export type TipoOrganizacao =
  | 'GOVERNO'
  | 'ACADEMIA'
  | 'EMPRESA_PRIVADA'
  | 'ORGANIZACAO_INTERNACIONAL'
  | 'ASSOCIACAO'
  | 'STARTUP'
  | 'TERCEIRO_SETOR'

export type StatusParceiro = 'ATIVO' | 'EM_ANALISE' | 'BLOQUEADO' | 'INATIVO'

export interface Parceiro {
  id: string
  razaoSocial: string
  nomeFantasia?: string | null
  cnpj: string // 14 dígitos, sem máscara
  tipoOrganizacao: TipoOrganizacao
  segmentoAtuacao?: string | null
  porte?: string | null
  emailContato?: string | null
  telefone?: string | null
  enderecoCompleto?: string | null
  responsaveis?: string | null
  status: string
  dataCadastro: string
}

// Corpo enviado em POST/PUT (o backend gera id e dataCadastro)
export type ParceiroPayload = Omit<Parceiro, 'id' | 'dataCadastro'>

export interface HistoricoParceiro {
  id: string
  tipo: string
  descricao: string
  dataEvento: string
}

export interface HistoricoPayload {
  tipo: string
  descricao: string
}

// Formato de erro padronizado pelo GlobalExceptionHandler
export interface ApiErrorBody {
  status: number
  mensagem: string
  erros?: Record<string, string>
}
