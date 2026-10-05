import type { StatusParceiro, TipoOrganizacao } from '../types/parceiro'

export const TIPO_ORGANIZACAO_OPTIONS: { label: string; value: TipoOrganizacao }[] = [
  { label: 'Governo', value: 'GOVERNO' },
  { label: 'Academia', value: 'ACADEMIA' },
  { label: 'Empresa Privada', value: 'EMPRESA_PRIVADA' },
  { label: 'Organização Internacional', value: 'ORGANIZACAO_INTERNACIONAL' },
  { label: 'Associação', value: 'ASSOCIACAO' },
  { label: 'Startup', value: 'STARTUP' },
  { label: 'Entidade do Terceiro Setor', value: 'TERCEIRO_SETOR' },
]

export const PORTE_OPTIONS = [
  { label: 'Micro', value: 'MICRO' },
  { label: 'Pequeno', value: 'PEQUENO' },
  { label: 'Médio', value: 'MEDIO' },
  { label: 'Grande', value: 'GRANDE' },
]

export const AREAS_ATUACAO = [
  'Tecnologia',
  'Pesquisa',
  'Educação',
  'Saúde',
  'Energia',
  'Agro',
  'Sustentabilidade',
]

export const STATUS_OPTIONS: { label: string; value: StatusParceiro }[] = [
  { label: 'Ativo', value: 'ATIVO' },
  { label: 'Em Avaliação', value: 'EM_ANALISE' },
  { label: 'Bloqueado', value: 'BLOQUEADO' },
  { label: 'Inativo', value: 'INATIVO' },
]

export const STATUS_SEVERITY: Record<string, 'success' | 'warning' | 'danger' | 'secondary'> = {
  ATIVO: 'success',
  EM_ANALISE: 'warning',
  BLOQUEADO: 'danger',
  INATIVO: 'secondary',
}

// RF003 – tipos de evento do histórico do parceiro
export const TIPO_HISTORICO_OPTIONS = [
  { label: 'Interação realizada', value: 'INTERACAO' },
  { label: 'Alteração cadastral', value: 'ALTERACAO_CADASTRAL' },
  { label: 'Acordo celebrado', value: 'ACORDO' },
  { label: 'Projeto executado', value: 'PROJETO' },
]

export const UF_OPTIONS = [
  'AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO',
]

export function labelFrom(options: { label: string; value: string }[], value?: string | null) {
  return options.find((o) => o.value === value)?.label ?? value ?? '—'
}
